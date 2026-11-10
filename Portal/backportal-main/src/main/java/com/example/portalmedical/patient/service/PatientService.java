package com.example.portalmedical.patient.service;

import java.util.List;

import com.example.portalmedical.patient.entity.PatientEntity;

public interface PatientService {
	PatientEntity ajout(PatientEntity patient);
	PatientEntity update(PatientEntity patient);
	List<PatientEntity> getall ();
	PatientEntity getbyid(Long id);
	void delete(Long id);

}
