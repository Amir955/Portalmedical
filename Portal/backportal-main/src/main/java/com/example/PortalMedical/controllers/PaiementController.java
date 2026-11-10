package com.example.PortalMedical.controllers;

import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.example.PortalMedical.Repositories.PaiementRepository;
import com.example.PortalMedical.Repositories.PatientRepository;
import com.example.PortalMedical.Repositories.RendezVousRepository;
import com.example.PortalMedical.Repositories.UserRepository;
import com.example.PortalMedical.Services.MedicalPdfService;
import com.example.PortalMedical.Services.PlanningService;
import com.example.PortalMedical.Services.PracticeScope;
import com.example.PortalMedical.enteties.PaiementEntity;
import com.example.PortalMedical.enteties.PatientEntity;
import com.example.PortalMedical.enteties.RendezVousEntity;
import com.example.PortalMedical.enteties.UserEntity;

/** Payments of the practice: checkout of an appointment, day/month lists and totals, receipts. */
@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/paiements")
public class PaiementController {

	private static final List<String> MODES = Arrays.asList("especes", "cheque", "carte", "virement");

	@Autowired
	private PracticeScope practiceScope;
	@Autowired
	private PaiementRepository paiementRepository;
	@Autowired
	private RendezVousRepository rendezVousRepository;
	@Autowired
	private PatientRepository patientRepository;
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private PlanningService planningService;
	@Autowired
	private MedicalPdfService pdfService;

	/** Request body of a checkout. */
	public static class Encaissement {
		public Double montant;
		public String mode;
		public String note;
	}

	/** The practice's default fee, to pre-fill the checkout. */
	@GetMapping("/tarif")
	public Map<String, Object> tarif() {
		Long doctorId = practiceScope.currentDoctorId();
		return Collections.singletonMap("tarif", doctorId == null ? null : planningService.tarif(doctorId));
	}

	/** Records (or corrects) the payment of an appointment and marks the visit as finished. */
	@PostMapping("/rendezvous/{rdvId}")
	public PaiementEntity encaisser(@PathVariable long rdvId, @RequestBody Encaissement body) {
		RendezVousEntity rdv = rendezVousRepository.findById(rdvId).orElse(null);
		if (rdv == null || rdv.getUser() == null || !practiceScope.canAccessDoctor(rdv.getUser().getId())) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND);
		}
		if (body == null || body.montant == null || body.montant < 0 || body.montant > 100000) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid amount");
		}
		String mode = body.mode != null && MODES.contains(body.mode) ? body.mode : "especes";

		PaiementEntity paiement = paiementRepository.findFirstByRendezVousId(rdvId);
		if (paiement == null) {
			paiement = new PaiementEntity();
			paiement.setRendezVousId(rdvId);
			paiement.setDatePaiement(new Date());
		}
		paiement.setMedecinId(rdv.getUser().getId());
		paiement.setPatientId(rdv.getPatient() == null ? null : rdv.getPatient().getId());
		paiement.setPatientNom(patientName(rdv));
		paiement.setMontant(Math.round(body.montant * 1000d) / 1000d);
		paiement.setMode(mode);
		paiement.setNote(body.note == null || body.note.trim().isEmpty() ? null : body.note.trim());
		UserEntity me = practiceScope.currentUser();
		paiement.setEncaissePar(me == null ? null : (nz(me.getPrenom()) + " " + nz(me.getNom())).trim());
		paiement = paiementRepository.save(paiement);

		rdv.setStatutVisite("termine");
		rendezVousRepository.save(rdv);
		return paiement;
	}

	/** Payments between two dates (both included), newest first. */
	@GetMapping
	public List<PaiementEntity> list(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate du,
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate au) {
		return between(du, au);
	}

	/** Totals between two dates: amount, number of payments, amount per payment method. */
	@GetMapping("/resume")
	public Map<String, Object> resume(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate du,
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate au) {
		List<PaiementEntity> paiements = between(du, au);
		Map<String, Double> parMode = new LinkedHashMap<>();
		MODES.forEach(mode -> parMode.put(mode, 0d));
		double total = 0;
		for (PaiementEntity p : paiements) {
			total += p.getMontant();
			parMode.merge(p.getMode() == null ? "especes" : p.getMode(), p.getMontant(), Double::sum);
		}
		Map<String, Object> result = new LinkedHashMap<>();
		result.put("total", Math.round(total * 1000d) / 1000d);
		result.put("nombre", paiements.size());
		result.put("parMode", parMode);
		return result;
	}

	@GetMapping(value = "/{id}/recu", produces = MediaType.APPLICATION_PDF_VALUE)
	public ResponseEntity<byte[]> recu(@PathVariable long id) throws IOException {
		PaiementEntity paiement = require(id);
		UserEntity doctor = userRepository.findById(paiement.getMedecinId())
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
		PatientEntity patient = paiement.getPatientId() == null ? null
				: patientRepository.findById(paiement.getPatientId()).orElse(null);
		if (patient == null) {
			// Appointment without a patient record: use the name kept on the payment.
			patient = new PatientEntity();
			patient.setPrenom(paiement.getPatientNom());
		}
		byte[] pdf = pdfService.buildRecu(doctor, patient, paiement);
		return ResponseEntity.ok()
				.contentType(MediaType.APPLICATION_PDF)
				.header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + paiement.getNumeroRecu() + ".pdf\"")
				.body(pdf);
	}

	@DeleteMapping("/{id}")
	public void delete(@PathVariable long id) {
		PaiementEntity paiement = require(id);
		paiementRepository.delete(paiement);
		if (paiement.getRendezVousId() != null) {
			rendezVousRepository.findById(paiement.getRendezVousId()).ifPresent(rdv -> {
				if ("termine".equals(rdv.getStatutVisite())) {
					rdv.setStatutVisite("en_consultation");
					rendezVousRepository.save(rdv);
				}
			});
		}
	}

	// ------------------------------------------------------------------ helpers

	private List<PaiementEntity> between(LocalDate du, LocalDate au) {
		if (au.isBefore(du)) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The end date is before the start date");
		}
		ZoneId zone = ZoneId.systemDefault();
		Date start = Date.from(du.atStartOfDay(zone).toInstant());
		Date end = Date.from(au.plusDays(1).atStartOfDay(zone).toInstant());
		Long doctorId = practiceScope.currentDoctorId();
		return doctorId == null
				? paiementRepository.findByDatePaiementGreaterThanEqualAndDatePaiementLessThanOrderByDatePaiementDesc(start, end)
				: paiementRepository.findByMedecinIdAndDatePaiementGreaterThanEqualAndDatePaiementLessThanOrderByDatePaiementDesc(doctorId, start, end);
	}

	private PaiementEntity require(long id) {
		PaiementEntity paiement = paiementRepository.findById(id).orElse(null);
		if (paiement == null || !practiceScope.canAccessDoctor(paiement.getMedecinId())) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND);
		}
		return paiement;
	}

	private static String patientName(RendezVousEntity rdv) {
		String prenom = rdv.getPatient() != null ? rdv.getPatient().getPrenom() : rdv.getPatientPrenom();
		String nom = rdv.getPatient() != null ? rdv.getPatient().getNom() : rdv.getPatientNom();
		String name = (nz(prenom) + " " + nz(nom)).trim();
		return name.isEmpty() ? rdv.getPatientEmail() : name;
	}

	private static String nz(String value) {
		return value == null ? "" : value.trim();
	}
}
