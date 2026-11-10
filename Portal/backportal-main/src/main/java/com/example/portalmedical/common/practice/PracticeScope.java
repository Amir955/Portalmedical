package com.example.portalmedical.common.practice;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.example.portalmedical.user.repository.UserRepository;
import com.example.portalmedical.patient.entity.PatientEntity;
import com.example.portalmedical.user.entity.Role;
import com.example.portalmedical.user.entity.UserEntity;

/**
 * Which doctor's practice the signed-in user works in: a doctor works on their own data,
 * a secretary on her doctor's. Records outside the practice are answered as "not found"
 * so their existence is not revealed.
 *
 * Older secretaries created before doctors managed their team have no doctor: they keep
 * seeing everything until their doctor adds them again.
 */
@Service
public class PracticeScope {

	@Autowired
	private UserRepository userRepository;

	/** The signed-in user, or null for an anonymous call. */
	public UserEntity currentUser() {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken
				|| auth.getPrincipal() == null) {
			return null;
		}
		return userRepository.findByEmail(auth.getPrincipal().toString());
	}

	/** Doctor id of the user's practice, or null when the user is not tied to one. */
	public static Long doctorIdOf(UserEntity user) {
		if (user == null) {
			return null;
		}
		if (user.getRole() == Role.Medecin) {
			return user.getId();
		}
		return user.getRole() == Role.Secretaire ? user.getMedecinId() : null;
	}

	public Long currentDoctorId() {
		return doctorIdOf(currentUser());
	}

	public boolean canAccessDoctor(Long doctorId) {
		Long scope = currentDoctorId();
		return scope == null || scope.equals(doctorId);
	}

	/** Throws 404 unless the doctor belongs to the user's practice. */
	public void requireDoctor(Long doctorId) {
		if (!canAccessDoctor(doctorId)) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND);
		}
	}

	/** Patients without a doctor are older shared records, visible to every practice. */
	public boolean canAccessPatient(PatientEntity patient) {
		if (patient == null) {
			return false;
		}
		Long scope = currentDoctorId();
		return scope == null || patient.getMedecinId() == null || scope.equals(patient.getMedecinId());
	}

	public PatientEntity requirePatient(PatientEntity patient) {
		if (!canAccessPatient(patient)) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND);
		}
		return patient;
	}
}
