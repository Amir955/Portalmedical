package com.example.PortalMedical.Repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.PortalMedical.enteties.OrdonnanceEntity;


public interface OrdonnanceRepository extends JpaRepository <OrdonnanceEntity,Long>{
	List<OrdonnanceEntity> findByUserId(long userId);
	List<OrdonnanceEntity> findByPatientId(long patientId);
	List<OrdonnanceEntity> findByUserIdAndPatientId(long userId, long patientId);

}
