package com.example.PortalMedical.Repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.PortalMedical.enteties.CertificatsMedicalEntity;


public interface CertificatsMedicalRepository extends JpaRepository <CertificatsMedicalEntity,Long>{
	List<CertificatsMedicalEntity> findByUserId(long userId);
	List<CertificatsMedicalEntity> findByPatientId(long patientId);
	List<CertificatsMedicalEntity> findByUserIdAndPatientId(long userId, long patientId);

}
