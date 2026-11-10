package com.example.PortalMedical.controllers;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.PortalMedical.Repositories.CertificatsMedicalRepository;
import com.example.PortalMedical.Services.ConsultationContextService;
import com.example.PortalMedical.enteties.CertificatsMedicalEntity;
import com.example.PortalMedical.enteties.PatientEntity;
import com.example.PortalMedical.enteties.UserEntity;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/consultation/certificats-medicals")
public class CertificatsMedicalControllers {

	@Autowired
	private CertificatsMedicalRepository certificatRepository;

	@Autowired
	private ConsultationContextService contextService;

	@PostMapping("/{patientId}")
	public CertificatsMedicalEntity ajouter(@PathVariable long patientId, @RequestBody CertificatsMedicalEntity certificat) {
		UserEntity doctor = contextService.getCurrentDoctor();
		PatientEntity patient = contextService.getPatient(patientId);
		certificat.setUser(doctor);
		certificat.setPatient(patient);
		if (certificat.getDatedecreation() == null) {
			certificat.setDatedecreation(new Date());
		}
		certificat.setIdCertificats(0);
		return certificatRepository.save(certificat);
	}

	@GetMapping
	public List<CertificatsMedicalEntity> getAllForCurrentDoctor() {
		return certificatRepository.findByUserId(contextService.getCurrentDoctor().getId());
	}

	@GetMapping("/patient/{patientId}")
	public List<CertificatsMedicalEntity> getAllForCurrentDoctorAndPatient(@PathVariable long patientId) {
		UserEntity doctor = contextService.getCurrentDoctor();
		return certificatRepository.findByUserIdAndPatientId(doctor.getId(), patientId);
	}

	@GetMapping("/{id}")
	public CertificatsMedicalEntity getById(@PathVariable long id) {
		return contextService.requireOwned(certificatRepository.findById(id), CertificatsMedicalEntity::getUser);
	}

	@PutMapping("/{id}/{patientId}")
	public CertificatsMedicalEntity update(@PathVariable long id, @PathVariable long patientId,
			@RequestBody CertificatsMedicalEntity certificat) {
		CertificatsMedicalEntity existing = contextService.requireOwned(certificatRepository.findById(id), CertificatsMedicalEntity::getUser);
		UserEntity doctor = contextService.getCurrentDoctor();
		PatientEntity patient = contextService.getPatient(patientId);
		existing.setDatedecreation(certificat.getDatedecreation() != null ? certificat.getDatedecreation() : existing.getDatedecreation());
		existing.setNombre_de_jour_repos(certificat.getNombre_de_jour_repos());
		existing.setDaterepos(certificat.getDaterepos());
		existing.setCommantaire(certificat.getCommantaire());
		existing.setUser(doctor);
		existing.setPatient(patient);
		return certificatRepository.save(existing);
	}

	@DeleteMapping("/{id}")
	public void delete(@PathVariable long id) {
		certificatRepository.delete(contextService.requireOwned(certificatRepository.findById(id), CertificatsMedicalEntity::getUser));
	}
}
