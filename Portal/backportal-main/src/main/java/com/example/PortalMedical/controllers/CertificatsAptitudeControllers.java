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

import com.example.PortalMedical.Repositories.CertificatsAptitudeRepository;
import com.example.PortalMedical.Services.ConsultationContextService;
import com.example.PortalMedical.enteties.CertificatsAptitudeEntity;
import com.example.PortalMedical.enteties.PatientEntity;
import com.example.PortalMedical.enteties.UserEntity;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/consultation/certificats-aptitudes")
public class CertificatsAptitudeControllers {

	@Autowired
	private CertificatsAptitudeRepository certificatRepository;

	@Autowired
	private ConsultationContextService contextService;

	@PostMapping("/{patientId}")
	public CertificatsAptitudeEntity ajouter(@PathVariable long patientId, @RequestBody CertificatsAptitudeEntity certificat) {
		UserEntity doctor = contextService.getCurrentDoctor();
		PatientEntity patient = contextService.getPatient(patientId);
		certificat.setUser(doctor);
		certificat.setPatient(patient);
		if (certificat.getDatedecreation() == null) {
			certificat.setDatedecreation(new Date());
		}
		certificat.setIdCertificatsaptitude(0);
		return certificatRepository.save(certificat);
	}

	@GetMapping
	public List<CertificatsAptitudeEntity> getAllForCurrentDoctor() {
		return certificatRepository.findByUserId(contextService.getCurrentDoctor().getId());
	}

	@GetMapping("/patient/{patientId}")
	public List<CertificatsAptitudeEntity> getAllForCurrentDoctorAndPatient(@PathVariable long patientId) {
		UserEntity doctor = contextService.getCurrentDoctor();
		return certificatRepository.findByUserIdAndPatientId(doctor.getId(), patientId);
	}

	@GetMapping("/{id}")
	public CertificatsAptitudeEntity getById(@PathVariable long id) {
		return contextService.requireOwned(certificatRepository.findById(id), CertificatsAptitudeEntity::getUser);
	}

	@PutMapping("/{id}/{patientId}")
	public CertificatsAptitudeEntity update(@PathVariable long id, @PathVariable long patientId,
			@RequestBody CertificatsAptitudeEntity certificat) {
		CertificatsAptitudeEntity existing = contextService.requireOwned(certificatRepository.findById(id), CertificatsAptitudeEntity::getUser);
		UserEntity doctor = contextService.getCurrentDoctor();
		PatientEntity patient = contextService.getPatient(patientId);
		existing.setDatedecreation(certificat.getDatedecreation() != null ? certificat.getDatedecreation() : existing.getDatedecreation());
		existing.setProfession(certificat.getProfession());
		existing.setEtat_de_sante(certificat.getEtat_de_sante());
		existing.setCommantaire(certificat.getCommantaire());
		existing.setUser(doctor);
		existing.setPatient(patient);
		return certificatRepository.save(existing);
	}

	@DeleteMapping("/{id}")
	public void delete(@PathVariable long id) {
		certificatRepository.delete(contextService.requireOwned(certificatRepository.findById(id), CertificatsAptitudeEntity::getUser));
	}
}
