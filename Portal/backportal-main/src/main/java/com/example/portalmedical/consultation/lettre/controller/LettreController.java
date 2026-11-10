package com.example.portalmedical.consultation.lettre.controller;

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

import com.example.portalmedical.consultation.lettre.repository.LettreRepository;
import com.example.portalmedical.consultation.service.ConsultationContextService;
import com.example.portalmedical.consultation.lettre.entity.LettreConfrereEntity;
import com.example.portalmedical.patient.entity.PatientEntity;
import com.example.portalmedical.user.entity.UserEntity;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/consultation/lettres")
public class LettreController {

	@Autowired
	private LettreRepository lettreRepository;

	@Autowired
	private ConsultationContextService contextService;

	@PostMapping("/{patientId}")
	public LettreConfrereEntity ajouter(@PathVariable long patientId, @RequestBody LettreConfrereEntity lettre) {
		UserEntity doctor = contextService.getCurrentDoctor();
		PatientEntity patient = contextService.getPatient(patientId);
		lettre.setUser(doctor);
		lettre.setPatient(patient);
		if (lettre.getDatedecreation() == null) {
			lettre.setDatedecreation(new Date());
		}
		lettre.setIdlettre(0);
		return lettreRepository.save(lettre);
	}

	@GetMapping
	public List<LettreConfrereEntity> getAllForCurrentDoctor() {
		return lettreRepository.findByUserId(contextService.getCurrentDoctor().getId());
	}

	@GetMapping("/patient/{patientId}")
	public List<LettreConfrereEntity> getAllForCurrentDoctorAndPatient(@PathVariable long patientId) {
		UserEntity doctor = contextService.getCurrentDoctor();
		return lettreRepository.findByUserIdAndPatientId(doctor.getId(), patientId);
	}

	@GetMapping("/{id}")
	public LettreConfrereEntity getById(@PathVariable long id) {
		return contextService.requireOwned(lettreRepository.findById(id), LettreConfrereEntity::getUser);
	}

	@PutMapping("/{id}/{patientId}")
	public LettreConfrereEntity update(@PathVariable long id, @PathVariable long patientId,
			@RequestBody LettreConfrereEntity lettre) {
		LettreConfrereEntity existing = contextService.requireOwned(lettreRepository.findById(id), LettreConfrereEntity::getUser);
		UserEntity doctor = contextService.getCurrentDoctor();
		PatientEntity patient = contextService.getPatient(patientId);
		existing.setDatedecreation(lettre.getDatedecreation() != null ? lettre.getDatedecreation() : existing.getDatedecreation());
		existing.setNondeconfrere(lettre.getNondeconfrere());
		existing.setPrenomdeconfrere(lettre.getPrenomdeconfrere());
		existing.setSpecialitedeconfrere(lettre.getSpecialitedeconfrere());
		existing.setAdressedeconfere(lettre.getAdressedeconfere());
		existing.setTelephoneconfrere(lettre.getTelephoneconfrere());
		existing.setGSMConfrere(lettre.getGSMConfrere());
		existing.setEtatdesantedupatient(lettre.getEtatdesantedupatient());
		existing.setUser(doctor);
		existing.setPatient(patient);
		return lettreRepository.save(existing);
	}

	@DeleteMapping("/{id}")
	public void delete(@PathVariable long id) {
		lettreRepository.delete(contextService.requireOwned(lettreRepository.findById(id), LettreConfrereEntity::getUser));
	}
}
