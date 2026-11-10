package com.example.PortalMedical.Services;

import java.util.List;

import com.example.PortalMedical.enteties.CertificatsMedicalEntity;

public interface CertificatsmedicalServices {
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
