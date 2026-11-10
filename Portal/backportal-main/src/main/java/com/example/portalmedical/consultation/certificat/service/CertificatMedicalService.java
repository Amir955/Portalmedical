package com.example.portalmedical.consultation.certificat.service;

import java.util.List;

import com.example.portalmedical.consultation.certificat.entity.CertificatsMedicalEntity;

public interface CertificatMedicalService {
	 public CertificatsMedicalEntity ajouterCertificat(CertificatsMedicalEntity Certificat);
	 List<CertificatsMedicalEntity> retrieveAllCertificat();
	 public void deleteCertificatById(long idCertificats);
	 public CertificatsMedicalEntity updateCertificat(CertificatsMedicalEntity Certificat);
	 public List<CertificatsMedicalEntity> getAllCertificatByUserId() ;
	 public List<CertificatsMedicalEntity> getAllCertificatList();
	 public void deleteCertificatById1(long idCertificats);
	 public List<CertificatsMedicalEntity> getAllCertificatByUserIdWaiting();
	 public List<CertificatsMedicalEntity> getAllCertificatByUserIdWaiting2();
}
