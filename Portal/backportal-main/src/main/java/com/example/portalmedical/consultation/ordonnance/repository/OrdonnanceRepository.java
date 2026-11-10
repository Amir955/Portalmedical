package com.example.portalmedical.consultation.ordonnance.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.portalmedical.consultation.ordonnance.entity.OrdonnanceEntity;


public interface OrdonnanceRepository extends JpaRepository <OrdonnanceEntity,Long>{
	List<OrdonnanceEntity> findByUserId(long userId);
	List<OrdonnanceEntity> findByPatientId(long patientId);
	List<OrdonnanceEntity> findByUserIdAndPatientId(long userId, long patientId);

}
