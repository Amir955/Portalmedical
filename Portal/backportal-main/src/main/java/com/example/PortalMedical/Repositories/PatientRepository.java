package com.example.PortalMedical.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.PortalMedical.enteties.PatientEntity;


@Repository
public interface PatientRepository extends JpaRepository <PatientEntity,Long> {

	PatientEntity findByNomAndPrenomAndEmail(String nom, String prenom, String email);

	PatientEntity findFirstByEmailIgnoreCase(String email);

	/** A practice's patients plus the older records not tied to any doctor. */
	java.util.List<PatientEntity> findByMedecinIdOrMedecinIdIsNull(Long medecinId);

	PatientEntity findFirstByEmailIgnoreCaseAndMedecinId(String email, Long medecinId);

	java.util.List<PatientEntity> findByMedecinIdIsNull();

}
