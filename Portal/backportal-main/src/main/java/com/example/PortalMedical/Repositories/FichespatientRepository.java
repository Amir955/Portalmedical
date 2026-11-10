package com.example.PortalMedical.Repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.PortalMedical.enteties.FichepatientEntity;

public interface FichespatientRepository extends JpaRepository <FichepatientEntity,Long> {
	List<FichepatientEntity> findByUserId(long userId);
	List<FichepatientEntity> findByPatientId(long patientId);
	List<FichepatientEntity> findByUserIdAndPatientId(long userId, long patientId);

}
