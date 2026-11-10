package com.example.portalmedical.consultation.certificat.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.portalmedical.consultation.certificat.entity.CertificatsMedicalEntity;


public interface CertificatMedicalRepository extends JpaRepository <CertificatsMedicalEntity,Long>{
	List<CertificatsMedicalEntity> findByUserId(long userId);
	List<CertificatsMedicalEntity> findByPatientId(long patientId);
	List<CertificatsMedicalEntity> findByUserIdAndPatientId(long userId, long patientId);

}
