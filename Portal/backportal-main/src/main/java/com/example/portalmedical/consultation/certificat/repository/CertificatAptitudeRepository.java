package com.example.portalmedical.consultation.certificat.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.portalmedical.consultation.certificat.entity.CertificatsAptitudeEntity;

public interface CertificatAptitudeRepository extends JpaRepository <CertificatsAptitudeEntity,Long>{
	List<CertificatsAptitudeEntity> findByUserId(long userId);
	List<CertificatsAptitudeEntity> findByPatientId(long patientId);
	List<CertificatsAptitudeEntity> findByUserIdAndPatientId(long userId, long patientId);

}
