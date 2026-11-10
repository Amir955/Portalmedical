package com.example.portalmedical.consultation.certificat.service;

import java.util.List;

import com.example.portalmedical.consultation.certificat.entity.CertificatsAptitudeEntity;

public interface CertificatAptitudeService {
	 public CertificatsAptitudeEntity ajouterCertificatAptitude(CertificatsAptitudeEntity CertificatAptitude);
	 List<CertificatsAptitudeEntity> retrieveAllCertificatAptitude();
	 public void deleteCertificatAptitudeById(long idCertificatsaptitude);
	 public CertificatsAptitudeEntity updateCertificatAptitude(CertificatsAptitudeEntity CertificatAptitude);
	 public List<CertificatsAptitudeEntity> getAllCertificatAptitudeByUserId() ;
	 public List<CertificatsAptitudeEntity> getAllCertificatAptitudeList();
	 public void deleteCertificatAptitudeById1(long idCertificatsaptitude);
	 public List<CertificatsAptitudeEntity> getAllCertificatAptitudeByUserIdWaiting();
	 public List<CertificatsAptitudeEntity> getAllCertificatAptitudeByUserIdWaiting2();
}
