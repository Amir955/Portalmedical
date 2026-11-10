package com.example.portalmedical.patient.controller;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.example.portalmedical.patient.repository.PatientRepository;
import com.example.portalmedical.common.practice.PracticeScope;
import com.example.portalmedical.patient.entity.PatientEntity;

/** Patients of the signed-in user's practice (plus older records not tied to a doctor). */
@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/patient")
public class PatientController {

	@Autowired
	PatientRepository patientRepository;

	@Autowired
	PracticeScope practiceScope;

	@PostMapping("/ajout")
	public PatientEntity ajout(@RequestBody PatientEntity patient) {
		Long doctorId = practiceScope.currentDoctorId();
		PatientEntity existing = patientRepository.findByNomAndPrenomAndEmail(
				patient.getNom(), patient.getPrenom(), patient.getEmail());
		if (existing != null && practiceScope.canAccessPatient(existing)) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Patient already exists");
		}
		patient.setId(0);
		patient.setMedecinId(doctorId);
		if (patient.getCreationDate() == null) {
			patient.setCreationDate(new Date());
		}
		return patientRepository.save(patient);
	}

	@PutMapping("/update")
	public PatientEntity update(@RequestBody PatientEntity patient) {
		PatientEntity existing = require(patient.getId());
		// The practice a patient belongs to never changes through an edit.
		patient.setMedecinId(existing.getMedecinId());
		patient.setCreationDate(existing.getCreationDate());
		return patientRepository.save(patient);
	}

	@GetMapping("/getall")
	public List<PatientEntity> getall() {
		Long doctorId = practiceScope.currentDoctorId();
		return doctorId == null ? patientRepository.findAll() : patientRepository.findByMedecinIdOrMedecinIdIsNull(doctorId);
	}

	@GetMapping("/getbyid/{id}")
	public PatientEntity getByid(@PathVariable(name = "id") Long id) {
		return require(id);
	}

	@DeleteMapping("/delete/{id}")
	public void delete(@PathVariable(name = "id") Long id) {
		patientRepository.delete(require(id));
	}

	private PatientEntity require(Long id) {
		PatientEntity patient = id == null ? null : patientRepository.findById(id).orElse(null);
		return practiceScope.requirePatient(patient);
	}
}
