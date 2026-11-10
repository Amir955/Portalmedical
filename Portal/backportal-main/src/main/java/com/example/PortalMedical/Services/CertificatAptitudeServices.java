package com.example.PortalMedical.Services;

import java.util.List;

import com.example.PortalMedical.enteties.CertificatsAptitudeEntity;

public interface CertificatAptitudeServices {
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
