package com.example.PortalMedical.Services;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.example.PortalMedical.Repositories.CongeRepository;
import com.example.PortalMedical.Repositories.PlanningJourRepository;
import com.example.PortalMedical.Repositories.PlanningParametresRepository;
import com.example.PortalMedical.enteties.CongeEntity;
import com.example.PortalMedical.enteties.PlanningJourEntity;
import com.example.PortalMedical.enteties.PlanningParametresEntity;

/**
 * A doctor's opening hours, slot length and days off, and the bookable slots they give.
 * Until a doctor saves their own hours, the defaults below apply.
 */
@Service
public class PlanningService {

	public static final int DEFAULT_SLOT_MINUTES = 30;
	public static final List<Integer> ALLOWED_SLOT_MINUTES = Arrays.asList(10, 15, 20, 30, 45, 60);

	@Autowired
	private PlanningJourRepository jourRepository;

	@Autowired
	private PlanningParametresRepository parametresRepository;

	@Autowired
	private CongeRepository congeRepository;

	/** One weekday as sent to and received from the front end. */
	public static class Jour {
		public int jour;
		public boolean actif;
		public String debutMatin;
		public String finMatin;
		public String debutApresMidi;
		public String finApresMidi;
	}

	/** The whole weekly planning plus the upcoming days off. */
	public static class Planning {
		public int dureeCreneau;
		/** Default consultation fee (dinars), or null. */
		public Double tarifConsultation;
		public List<Jour> jours = new ArrayList<>();
		public List<CongeEntity> conges = new ArrayList<>();
	}

	/** Bookable times of a day, or why the practice is closed. */
	public static class Creneaux {
		public String date;
		public int dureeCreneau;
		public List<String> slots = new ArrayList<>();
		/** null when open; "conge" or "ferme" otherwise. */
		public String fermeture;
		public String motif;
	}

	// ------------------------------------------------------------------ read

	public Planning get(long medecinId) {
		Planning planning = new Planning();
		planning.dureeCreneau = slotMinutes(medecinId);
		planning.tarifConsultation = tarif(medecinId);
		for (int day = 1; day <= 7; day++) {
			planning.jours.add(toDto(dayRule(medecinId, day)));
		}
		planning.conges = congeRepository.findByMedecinIdAndDateFinGreaterThanEqualOrderByDateDebut(medecinId, LocalDate.now());
		return planning;
	}

	public Creneaux creneaux(long medecinId, LocalDate date) {
		Creneaux result = new Creneaux();
		result.date = date.toString();
		result.dureeCreneau = slotMinutes(medecinId);

		CongeEntity conge = congeOn(medecinId, date);
		if (conge != null) {
			result.fermeture = "conge";
			result.motif = conge.getMotif();
			return result;
		}
		PlanningJourEntity rule = dayRule(medecinId, date.getDayOfWeek().getValue());
		if (!rule.isActif()) {
			result.fermeture = "ferme";
			return result;
		}
		addSlots(result.slots, rule.getDebutMatin(), rule.getFinMatin(), result.dureeCreneau);
		addSlots(result.slots, rule.getDebutApresMidi(), rule.getFinApresMidi(), result.dureeCreneau);
		if (result.slots.isEmpty()) {
			result.fermeture = "ferme";
		}
		return result;
	}

	/** Public overview for the booking calendar: open weekdays and upcoming days off. */
	public java.util.Map<String, Object> calendrier(long medecinId) {
		List<Integer> joursOuverts = new ArrayList<>();
		int minutes = slotMinutes(medecinId);
		for (int day = 1; day <= 7; day++) {
			PlanningJourEntity rule = dayRule(medecinId, day);
			List<String> slots = new ArrayList<>();
			if (rule.isActif()) {
				addSlots(slots, rule.getDebutMatin(), rule.getFinMatin(), minutes);
				addSlots(slots, rule.getDebutApresMidi(), rule.getFinApresMidi(), minutes);
			}
			if (!slots.isEmpty()) {
				joursOuverts.add(day);
			}
		}
		List<java.util.Map<String, Object>> conges = new ArrayList<>();
		for (CongeEntity conge : congeRepository.findByMedecinIdAndDateFinGreaterThanEqualOrderByDateDebut(medecinId, LocalDate.now())) {
			java.util.Map<String, Object> item = new java.util.LinkedHashMap<>();
			item.put("dateDebut", conge.getDateDebut().toString());
			item.put("dateFin", conge.getDateFin().toString());
			item.put("motif", conge.getMotif());
			conges.add(item);
		}
		java.util.Map<String, Object> result = new java.util.LinkedHashMap<>();
		result.put("joursOuverts", joursOuverts);
		result.put("conges", conges);
		return result;
	}

	/** True when this exact time is a bookable slot of the doctor's planning. */
	public boolean isBookable(long medecinId, Date when) {
		if (when == null) {
			return false;
		}
		java.time.LocalDateTime local = when.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
		String time = String.format("%02d:%02d", local.getHour(), local.getMinute());
		return creneaux(medecinId, local.toLocalDate()).slots.contains(time);
	}

	public CongeEntity congeOn(long medecinId, LocalDate date) {
		return congeRepository.findFirstByMedecinIdAndDateDebutLessThanEqualAndDateFinGreaterThanEqual(medecinId, date, date);
	}

	// ------------------------------------------------------------------ write

	@Transactional
	public Planning save(long medecinId, Planning planning) {
		if (planning == null || planning.jours == null) {
			throw bad("Planning is required");
		}
		if (!ALLOWED_SLOT_MINUTES.contains(planning.dureeCreneau)) {
			throw bad("Slot length must be one of " + ALLOWED_SLOT_MINUTES);
		}
		for (Jour dto : planning.jours) {
			if (dto.jour < 1 || dto.jour > 7) {
				throw bad("Unknown weekday");
			}
			validateRange(dto.debutMatin, dto.finMatin);
			validateRange(dto.debutApresMidi, dto.finApresMidi);
			if (dto.actif && blank(dto.debutMatin) && blank(dto.debutApresMidi)) {
				throw bad("An open day needs morning or afternoon hours");
			}
			PlanningJourEntity entity = jourRepository.findFirstByMedecinIdAndJour(medecinId, dto.jour);
			if (entity == null) {
				entity = new PlanningJourEntity();
				entity.setMedecinId(medecinId);
				entity.setJour(dto.jour);
			}
			entity.setActif(dto.actif);
			entity.setDebutMatin(emptyToNull(dto.debutMatin));
			entity.setFinMatin(emptyToNull(dto.finMatin));
			entity.setDebutApresMidi(emptyToNull(dto.debutApresMidi));
			entity.setFinApresMidi(emptyToNull(dto.finApresMidi));
			jourRepository.save(entity);
		}

		PlanningParametresEntity settings = parametresRepository.findById(medecinId).orElseGet(PlanningParametresEntity::new);
		settings.setMedecinId(medecinId);
		settings.setDureeCreneau(planning.dureeCreneau);
		if (planning.tarifConsultation != null && (planning.tarifConsultation < 0 || planning.tarifConsultation > 100000)) {
			throw bad("Invalid consultation fee");
		}
		settings.setTarifConsultation(planning.tarifConsultation);
		parametresRepository.save(settings);
		return get(medecinId);
	}

	public CongeEntity addConge(long medecinId, CongeEntity conge) {
		if (conge == null || conge.getDateDebut() == null || conge.getDateFin() == null) {
			throw bad("Start and end dates are required");
		}
		if (conge.getDateFin().isBefore(conge.getDateDebut())) {
			throw bad("The end date is before the start date");
		}
		conge.setId(0);
		conge.setMedecinId(medecinId);
		conge.setMotif(blank(conge.getMotif()) ? null : conge.getMotif().trim());
		return congeRepository.save(conge);
	}

	public void deleteConge(long medecinId, long congeId) {
		CongeEntity conge = congeRepository.findById(congeId)
				.filter(c -> c.getMedecinId() == medecinId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
		congeRepository.delete(conge);
	}

	// ------------------------------------------------------------------ helpers

	/** The doctor's default consultation fee, or null when not set. */
	public Double tarif(long medecinId) {
		return parametresRepository.findById(medecinId).map(PlanningParametresEntity::getTarifConsultation).orElse(null);
	}

	private int slotMinutes(long medecinId) {
		return parametresRepository.findById(medecinId)
				.map(PlanningParametresEntity::getDureeCreneau)
				.filter(ALLOWED_SLOT_MINUTES::contains)
				.orElse(DEFAULT_SLOT_MINUTES);
	}

	/** Saved rule for that weekday, or the default (Mon–Fri 08:30–12:30 / 14:00–17:30). */
	private PlanningJourEntity dayRule(long medecinId, int day) {
		PlanningJourEntity saved = jourRepository.findFirstByMedecinIdAndJour(medecinId, day);
		if (saved != null) {
			return saved;
		}
		PlanningJourEntity rule = new PlanningJourEntity();
		rule.setMedecinId(medecinId);
		rule.setJour(day);
		rule.setActif(day <= 5);
		rule.setDebutMatin("08:30");
		rule.setFinMatin("12:30");
		rule.setDebutApresMidi("14:00");
		rule.setFinApresMidi("17:30");
		return rule;
	}

	private static Jour toDto(PlanningJourEntity rule) {
		Jour dto = new Jour();
		dto.jour = rule.getJour();
		dto.actif = rule.isActif();
		dto.debutMatin = rule.getDebutMatin();
		dto.finMatin = rule.getFinMatin();
		dto.debutApresMidi = rule.getDebutApresMidi();
		dto.finApresMidi = rule.getFinApresMidi();
		return dto;
	}

	/** Start times from start (included) while a full slot still fits before end. */
	private static void addSlots(List<String> slots, String start, String end, int minutes) {
		LocalTime from = parse(start);
		LocalTime to = parse(end);
		if (from == null || to == null) {
			return;
		}
		for (LocalTime t = from; !t.plusMinutes(minutes).isAfter(to) && t.isBefore(to); t = t.plusMinutes(minutes)) {
			slots.add(String.format("%02d:%02d", t.getHour(), t.getMinute()));
			if (t.plusMinutes(minutes).isBefore(t)) {
				break; // wrapped past midnight
			}
		}
	}

	private static void validateRange(String start, String end) {
		if (blank(start) && blank(end)) {
			return;
		}
		LocalTime from = parse(start);
		LocalTime to = parse(end);
		if (from == null || to == null) {
			throw bad("Times must be HH:mm");
		}
		if (!from.isBefore(to)) {
			throw bad("A start time must be before its end time");
		}
	}

	private static LocalTime parse(String value) {
		if (blank(value)) {
			return null;
		}
		try {
			return LocalTime.parse(value.trim());
		} catch (DateTimeParseException e) {
			return null;
		}
	}

	private static boolean blank(String value) {
		return value == null || value.trim().isEmpty();
	}

	private static String emptyToNull(String value) {
		return blank(value) ? null : value.trim();
	}

	private static ResponseStatusException bad(String message) {
		return new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
	}
}
