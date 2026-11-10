package com.example.PortalMedical.Repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.PortalMedical.enteties.CertificatsAptitudeEntity;

public interface CertificatsAptitudeRepository extends JpaRepository <CertificatsAptitudeEntity,Long>{
	List<CertificatsAptitudeEntity> findByUserId(long userId);
	List<CertificatsAptitudeEntity> findByPatientId(long patientId);
	List<CertificatsAptitudeEntity> findByUserIdAndPatientId(long userId, long patientId);

}
