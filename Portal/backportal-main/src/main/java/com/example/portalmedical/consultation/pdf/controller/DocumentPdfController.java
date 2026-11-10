package com.example.portalmedical.consultation.pdf.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.example.portalmedical.consultation.pdf.dto.DocumentPdfRequest;
import com.example.portalmedical.consultation.service.ConsultationContextService;
import com.example.portalmedical.consultation.pdf.service.MedicalPdfService;
import com.example.portalmedical.patient.entity.PatientEntity;
import com.example.portalmedical.user.entity.UserEntity;

/** PDF generation for certificates and letters (prescriptions live in OrdonnanceController). */
@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/consultation/documents")
public class DocumentPdfController {

	@Autowired
	private ConsultationContextService contextService;

	@Autowired
	private MedicalPdfService medicalPdfService;

	@PostMapping(value = "/patient/{patientId}/{type}/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
	public ResponseEntity<byte[]> generate(@PathVariable long patientId, @PathVariable String type,
			@RequestBody DocumentPdfRequest request) throws Exception {
		UserEntity doctor = contextService.getCurrentDoctor();
		PatientEntity patient = contextService.getPatient(patientId);

		byte[] pdf;
		String prefix;
		switch (type) {
		case "certificat-medical":
			pdf = medicalPdfService.buildCertificatMedical(doctor, patient, request);
			prefix = "certificat-medical";
			break;
		case "certificat-aptitude":
			pdf = medicalPdfService.buildCertificatAptitude(doctor, patient, request);
			prefix = "certificat-aptitude";
			break;
		case "lettre":
			pdf = medicalPdfService.buildLettre(doctor, patient, request);
			prefix = "lettre-confrere";
			break;
		default:
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Unknown document type");
		}

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_PDF);
		headers.setContentDisposition(ContentDisposition.attachment()
				.filename(prefix + "-" + patient.getNom() + "-" + patient.getPrenom() + ".pdf")
				.build());
		return ResponseEntity.ok().headers(headers).body(pdf);
	}
}
