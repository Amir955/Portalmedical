package com.example.portalmedical.consultation.fiche.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.portalmedical.consultation.fiche.entity.FichepatientEntity;

public interface FichePatientRepository extends JpaRepository <FichepatientEntity,Long> {
	List<FichepatientEntity> findByUserId(long userId);
	List<FichepatientEntity> findByPatientId(long patientId);
	List<FichepatientEntity> findByUserIdAndPatientId(long userId, long patientId);

}
