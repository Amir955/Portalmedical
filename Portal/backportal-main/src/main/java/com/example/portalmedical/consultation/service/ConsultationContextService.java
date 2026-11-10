package com.example.portalmedical.consultation.service;


import com.example.portalmedical.common.practice.PracticeScope;
import java.util.Optional;
import java.util.function.Function;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.example.portalmedical.patient.repository.PatientRepository;
import com.example.portalmedical.user.repository.UserRepository;
import com.example.portalmedical.patient.entity.PatientEntity;
import com.example.portalmedical.user.entity.UserEntity;

@Service
public class ConsultationContextService {

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private PatientRepository patientRepository;
	@Autowired
	private PracticeScope practiceScope;

	public UserEntity getCurrentDoctor() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null || authentication.getPrincipal() == null) {
			throw new RuntimeException("No authenticated doctor found");
		}
		String email = authentication.getPrincipal().toString();
		UserEntity user = userRepository.findByEmail(email);
		if (user == null) {
			throw new RuntimeException("Doctor not found for authenticated account");
		}
		return user;
	}

	/**
	 * Returns the record only if it belongs to the authenticated doctor.
	 * Records owned by someone else are reported as not found so their existence is not revealed.
	 */
	public <T> T requireOwned(Optional<T> record, Function<T, UserEntity> ownerOf) {
		long currentId = getCurrentDoctor().getId();
		return record
				.filter(r -> ownerOf.apply(r) != null && ownerOf.apply(r).getId() == currentId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
	}

	public PatientEntity getPatient(long patientId) {
		// Only a patient of the doctor's practice (or an older shared record).
		return practiceScope.requirePatient(patientRepository.findById(patientId).orElse(null));
	}
}
