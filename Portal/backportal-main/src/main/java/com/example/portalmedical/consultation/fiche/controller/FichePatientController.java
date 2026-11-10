package com.example.portalmedical.consultation.fiche.controller;

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

import com.example.portalmedical.consultation.fiche.repository.FichePatientRepository;
import com.example.portalmedical.consultation.service.ConsultationContextService;
import com.example.portalmedical.consultation.fiche.entity.FichepatientEntity;
import com.example.portalmedical.patient.entity.PatientEntity;
import com.example.portalmedical.user.entity.UserEntity;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/consultation/fiches")
public class FichePatientController {

	@Autowired
	private FichePatientRepository ficheRepository;

	@Autowired
	private ConsultationContextService contextService;

	@PostMapping("/{patientId}")
	public FichepatientEntity ajouter(@PathVariable long patientId, @RequestBody FichepatientEntity fiche) {
		UserEntity doctor = contextService.getCurrentDoctor();
		PatientEntity patient = contextService.getPatient(patientId);
		fiche.setUser(doctor);
		fiche.setPatient(patient);
		if (fiche.getDatedecreation() == null) {
			fiche.setDatedecreation(new Date());
		}
		fiche.setIdFiche(0);
		return ficheRepository.save(fiche);
	}

	@GetMapping
	public List<FichepatientEntity> getAllForCurrentDoctor() {
		return ficheRepository.findByUserId(contextService.getCurrentDoctor().getId());
	}

	@GetMapping("/patient/{patientId}")
	public List<FichepatientEntity> getAllForCurrentDoctorAndPatient(@PathVariable long patientId) {
		UserEntity doctor = contextService.getCurrentDoctor();
		return ficheRepository.findByUserIdAndPatientId(doctor.getId(), patientId);
	}

	@GetMapping("/{id}")
	public FichepatientEntity getById(@PathVariable long id) {
		return contextService.requireOwned(ficheRepository.findById(id), FichepatientEntity::getUser);
	}

	@PutMapping("/{id}/{patientId}")
	public FichepatientEntity update(@PathVariable long id, @PathVariable long patientId,
			@RequestBody FichepatientEntity fiche) {
		FichepatientEntity existing = contextService.requireOwned(ficheRepository.findById(id), FichepatientEntity::getUser);
		UserEntity doctor = contextService.getCurrentDoctor();
		PatientEntity patient = contextService.getPatient(patientId);
		existing.setDatedecreation(fiche.getDatedecreation() != null ? fiche.getDatedecreation() : existing.getDatedecreation());
		existing.setMatriculecnam(fiche.getMatriculecnam());
		existing.setCausedeconsultation(fiche.getCausedeconsultation());
		existing.setExamen(fiche.getExamen());
		existing.setSuivie(fiche.getSuivie());
		existing.setUser(doctor);
		existing.setPatient(patient);
		return ficheRepository.save(existing);
	}

	@DeleteMapping("/{id}")
	public void delete(@PathVariable long id) {
		ficheRepository.delete(contextService.requireOwned(ficheRepository.findById(id), FichepatientEntity::getUser));
	}
}
