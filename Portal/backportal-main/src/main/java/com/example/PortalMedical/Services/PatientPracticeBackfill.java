package com.example.PortalMedical.Services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.example.PortalMedical.Repositories.PatientRepository;
import com.example.PortalMedical.Repositories.RendezVousRepository;
import com.example.PortalMedical.enteties.PatientEntity;
import com.example.PortalMedical.enteties.RendezVousEntity;

/**
 * Patients created before practices existed have no doctor. At startup, each one is given
 * the doctor of their most recent appointment. Patients without any appointment stay
 * shared until a doctor edits them. Safe to run on every start: it only touches patients
 * that still have no doctor.
 */
@Component
public class PatientPracticeBackfill implements ApplicationRunner {

	private static final Logger LOG = LoggerFactory.getLogger(PatientPracticeBackfill.class);

	@Autowired
	private PatientRepository patientRepository;

	@Autowired
	private RendezVousRepository rendezVousRepository;

	@Override
	@Transactional
	public void run(ApplicationArguments args) {
		int assigned = 0;
		int shared = 0;
		for (PatientEntity patient : patientRepository.findByMedecinIdIsNull()) {
			RendezVousEntity last = rendezVousRepository.findFirstByPatient_IdOrderByDateOfAptDesc(patient.getId());
			if (last != null && last.getUser() != null) {
				patient.setMedecinId(last.getUser().getId());
				patientRepository.save(patient);
				assigned++;
			} else {
				shared++;
			}
		}
		if (assigned > 0 || shared > 0) {
			LOG.info("Patients linked to their doctor: {} ; still shared (no appointment): {}", assigned, shared);
		}
	}
}
