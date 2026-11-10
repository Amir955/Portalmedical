package com.example.portalmedical.consultation.ordonnance.controller;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.portalmedical.consultation.pdf.dto.OrdonnancePdfRequest;
import com.example.portalmedical.consultation.ordonnance.repository.OrdonnanceRepository;
import com.example.portalmedical.consultation.service.ConsultationContextService;
import com.example.portalmedical.consultation.pdf.service.MedicalPdfService;
import com.example.portalmedical.consultation.ordonnance.entity.OrdonnanceEntity;
import com.example.portalmedical.patient.entity.PatientEntity;
import com.example.portalmedical.user.entity.UserEntity;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/consultation/ordonnances")
public class OrdonnanceController {

	@Autowired
	private OrdonnanceRepository ordonnanceRepository;

	@Autowired
	private ConsultationContextService contextService;

	@Autowired
	private MedicalPdfService medicalPdfService;

	@PostMapping("/{patientId}")
	public OrdonnanceEntity ajouter(@PathVariable long patientId, @RequestBody OrdonnanceEntity ordonnance) {
		UserEntity doctor = contextService.getCurrentDoctor();
		PatientEntity patient = contextService.getPatient(patientId);
		ordonnance.setUser(doctor);
		ordonnance.setPatient(patient);
		if (ordonnance.getOrdonnance() == null) {
			ordonnance.setOrdonnance(new Date());
		}
		ordonnance.setIdOrdonnance(0);
		return ordonnanceRepository.save(ordonnance);
	}

	@GetMapping
	public List<OrdonnanceEntity> getAllForCurrentDoctor() {
		return ordonnanceRepository.findByUserId(contextService.getCurrentDoctor().getId());
	}

	@GetMapping("/patient/{patientId}")
	public List<OrdonnanceEntity> getAllForCurrentDoctorAndPatient(@PathVariable long patientId) {
		UserEntity doctor = contextService.getCurrentDoctor();
		return ordonnanceRepository.findByUserIdAndPatientId(doctor.getId(), patientId);
	}

	@GetMapping("/{id}")
	public OrdonnanceEntity getById(@PathVariable long id) {
		return contextService.requireOwned(ordonnanceRepository.findById(id), OrdonnanceEntity::getUser);
	}

	@PutMapping("/{id}/{patientId}")
	public OrdonnanceEntity update(@PathVariable long id, @PathVariable long patientId,
			@RequestBody OrdonnanceEntity ordonnance) {
		contextService.requireOwned(ordonnanceRepository.findById(id), OrdonnanceEntity::getUser);
		UserEntity doctor = contextService.getCurrentDoctor();
		PatientEntity patient = contextService.getPatient(patientId);
		ordonnance.setIdOrdonnance(id);
		ordonnance.setUser(doctor);
		ordonnance.setPatient(patient);
		if (ordonnance.getOrdonnance() == null) {
			ordonnance.setOrdonnance(new Date());
		}
		return ordonnanceRepository.save(ordonnance);
	}

	@DeleteMapping("/{id}")
	public void delete(@PathVariable long id) {
		ordonnanceRepository.delete(contextService.requireOwned(ordonnanceRepository.findById(id), OrdonnanceEntity::getUser));
	}

	@PostMapping(value = "/patient/{patientId}/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
	public ResponseEntity<byte[]> generatePdf(@PathVariable long patientId, @RequestBody OrdonnancePdfRequest request)
			throws Exception {
		UserEntity doctor = contextService.getCurrentDoctor();
		PatientEntity patient = contextService.getPatient(patientId);
		byte[] pdf = medicalPdfService.buildOrdonnance(doctor, patient, request);

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_PDF);
		headers.setContentDisposition(ContentDisposition.attachment()
				.filename("ordonnance-" + patient.getNom() + "-" + patient.getPrenom() + ".pdf")
				.build());

		return ResponseEntity.ok()
				.headers(headers)
				.body(pdf);
	}
}
