package com.example.portalmedical.statistics.controller;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.portalmedical.payment.repository.PaiementRepository;
import com.example.portalmedical.patient.repository.PatientRepository;
import com.example.portalmedical.common.practice.PracticeScope;
import com.example.portalmedical.appointment.service.RendezVousService;
import com.example.portalmedical.payment.entity.PaiementEntity;
import com.example.portalmedical.patient.entity.PatientEntity;
import com.example.portalmedical.appointment.entity.RendezVousEntity;
import com.example.portalmedical.appointment.entity.RendezVousEntity.StateApt;

/** Monthly figures of the practice: appointments, no-shows, new patients, revenue. */
@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/statistiques")
public class StatistiquesController {

	@Autowired
	private PracticeScope practiceScope;
	@Autowired
	private RendezVousService rendezVousServices;
	@Autowired
	private PatientRepository patientRepository;
	@Autowired
	private PaiementRepository paiementRepository;

	@GetMapping
	public Map<String, Object> statistiques(@RequestParam(defaultValue = "6") int mois) {
		int months = Math.max(1, Math.min(mois, 24));
		ZoneId zone = ZoneId.systemDefault();
		YearMonth last = YearMonth.now(zone);
		YearMonth first = last.minusMonths(months - 1L);

		Map<YearMonth, Map<String, Object>> byMonth = new LinkedHashMap<>();
		for (YearMonth m = first; !m.isAfter(last); m = m.plusMonths(1)) {
			Map<String, Object> row = new LinkedHashMap<>();
			row.put("cle", m.toString());
			row.put("libelle", m.getMonth().getDisplayName(TextStyle.SHORT, Locale.FRENCH) + " " + m.getYear());
			row.put("rendezVous", 0);
			row.put("honores", 0);
			row.put("absents", 0);
			row.put("annules", 0);
			row.put("demandesEnLigne", 0);
			row.put("nouveauxPatients", 0);
			row.put("chiffreAffaires", 0d);
			byMonth.put(m, row);
		}

		for (RendezVousEntity rdv : rendezVousServices.appointmentsFor(practiceScope.currentUser(), null)) {
			Map<String, Object> row = byMonth.get(monthOf(rdv.getDateOfApt(), zone));
			if (row == null) {
				continue;
			}
			if (rdv.getPatientNom() != null) {
				increment(row, "demandesEnLigne");
			}
			if (rdv.getStateapt() == StateApt.confirmed) {
				increment(row, "rendezVous");
				if ("absent".equals(rdv.getStatutVisite())) {
					increment(row, "absents");
				} else if ("termine".equals(rdv.getStatutVisite())) {
					increment(row, "honores");
				}
			} else if (rdv.getStateapt() == StateApt.cancelled) {
				increment(row, "annules");
			}
		}

		Long doctorId = practiceScope.currentDoctorId();
		List<PatientEntity> patients = doctorId == null ? patientRepository.findAll()
				: patientRepository.findByMedecinIdOrMedecinIdIsNull(doctorId);
		for (PatientEntity patient : patients) {
			Map<String, Object> row = byMonth.get(monthOf(patient.getCreationDate(), zone));
			if (row != null) {
				increment(row, "nouveauxPatients");
			}
		}

		Date start = Date.from(first.atDay(1).atStartOfDay(zone).toInstant());
		Date end = Date.from(last.plusMonths(1).atDay(1).atStartOfDay(zone).toInstant());
		List<PaiementEntity> paiements = doctorId == null
				? paiementRepository.findByDatePaiementGreaterThanEqualAndDatePaiementLessThanOrderByDatePaiementDesc(start, end)
				: paiementRepository.findByMedecinIdAndDatePaiementGreaterThanEqualAndDatePaiementLessThanOrderByDatePaiementDesc(doctorId, start, end);
		for (PaiementEntity p : paiements) {
			Map<String, Object> row = byMonth.get(monthOf(p.getDatePaiement(), zone));
			if (row != null) {
				row.put("chiffreAffaires", Math.round(((Double) row.get("chiffreAffaires") + p.getMontant()) * 1000d) / 1000d);
			}
		}

		int rendezVous = 0, honores = 0, absents = 0, nouveaux = 0;
		double chiffre = 0;
		for (Map<String, Object> row : byMonth.values()) {
			rendezVous += (Integer) row.get("rendezVous");
			honores += (Integer) row.get("honores");
			absents += (Integer) row.get("absents");
			nouveaux += (Integer) row.get("nouveauxPatients");
			chiffre += (Double) row.get("chiffreAffaires");
		}
		Map<String, Object> totaux = new LinkedHashMap<>();
		totaux.put("rendezVous", rendezVous);
		totaux.put("honores", honores);
		totaux.put("absents", absents);
		totaux.put("nouveauxPatients", nouveaux);
		totaux.put("chiffreAffaires", Math.round(chiffre * 1000d) / 1000d);
		// No-show rate among the visits whose outcome is known.
		totaux.put("tauxAbsence", honores + absents == 0 ? null : Math.round(absents * 1000d / (honores + absents)) / 10d);

		Map<String, Object> result = new LinkedHashMap<>();
		result.put("mois", new ArrayList<>(byMonth.values()));
		result.put("totaux", totaux);
		return result;
	}

	private static YearMonth monthOf(Date date, ZoneId zone) {
		if (date == null) {
			return null;
		}
		LocalDate local = date.toInstant().atZone(zone).toLocalDate();
		return YearMonth.from(local);
	}

	private static void increment(Map<String, Object> row, String key) {
		row.put(key, (Integer) row.get(key) + 1);
	}
}
