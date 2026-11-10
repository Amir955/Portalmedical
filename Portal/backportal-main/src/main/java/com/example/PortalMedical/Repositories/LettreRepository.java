package com.example.PortalMedical.Repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.PortalMedical.enteties.LettreConfrereEntity;

public interface LettreRepository extends JpaRepository <LettreConfrereEntity,Long>{
	List<LettreConfrereEntity> findByUserId(long userId);
	List<LettreConfrereEntity> findByPatientId(long patientId);
	List<LettreConfrereEntity> findByUserIdAndPatientId(long userId, long patientId);

}
