package com.example.portalmedical.consultation.lettre.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.portalmedical.consultation.lettre.entity.LettreConfrereEntity;

public interface LettreRepository extends JpaRepository <LettreConfrereEntity,Long>{
	List<LettreConfrereEntity> findByUserId(long userId);
	List<LettreConfrereEntity> findByPatientId(long patientId);
	List<LettreConfrereEntity> findByUserIdAndPatientId(long userId, long patientId);

}
