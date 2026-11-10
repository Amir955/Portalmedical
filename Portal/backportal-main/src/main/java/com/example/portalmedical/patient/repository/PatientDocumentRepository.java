package com.example.portalmedical.patient.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.example.portalmedical.patient.entity.PatientDocumentEntity;

public interface PatientDocumentRepository extends JpaRepository<PatientDocumentEntity, Long> {

	/** The doctor's documents for a patient, without loading the file contents. */
	@Query("select new com.example.portalmedical.patient.entity.PatientDocumentEntity(d.id, d.patientId, d.medecinId, d.nom, d.typeMime, d.taille, d.categorie, d.dateAjout) "
			+ "from PatientDocumentEntity d where d.patientId = ?1 and d.medecinId = ?2 order by d.dateAjout desc")
	List<PatientDocumentEntity> listFor(long patientId, long medecinId);
}
