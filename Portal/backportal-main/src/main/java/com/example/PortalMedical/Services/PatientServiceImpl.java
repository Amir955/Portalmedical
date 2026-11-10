package com.example.PortalMedical.Services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import com.example.PortalMedical.Repositories.PatientRepository;
import com.example.PortalMedical.enteties.PatientEntity;

@Service
public class PatientServiceImpl implements PatientService {
	@Autowired
	PatientRepository patientRepo;

	@Override
	public PatientEntity ajout(PatientEntity patient) {
		PatientEntity patFind = patientRepo.findByNomAndPrenomAndEmail(patient.getNom(),patient.getPrenom(),patient.getEmail());
		if(patFind!=null) {
			throw new RuntimeException("patient existe déja");
		}else {
			return patientRepo.save(patient);
		}

	}

	@Override
	public List<PatientEntity> getall() {
		return patientRepo.findAll(); 
	}

	@Override
	public PatientEntity getbyid(Long id) {
		// TODO Auto-generated method stub
		return patientRepo.findById(id).get();
	}

	@Override
	public PatientEntity update(PatientEntity patient) {
		// TODO Auto-generated method stub
		 return patientRepo.save(patient);
	}
	@Override
	public void delete(Long id) {
		// TODO Auto-generated method stub
		  patientRepo.deleteById(id);
	}
}
