package com.example.PortalMedical.Services;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.PortalMedical.Repositories.CertificatsAptitudeRepository;
import com.example.PortalMedical.enteties.CertificatsAptitudeEntity;
@Service
@Transactional
public class CertificatAptitudeServicesImpl implements CertificatAptitudeServices{
@Autowired
CertificatsAptitudeRepository certificatAptitude;
	@Override
	public CertificatsAptitudeEntity ajouterCertificatAptitude(CertificatsAptitudeEntity CertificatAptitude) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<CertificatsAptitudeEntity> retrieveAllCertificatAptitude() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void deleteCertificatAptitudeById(long idCertificatsaptitude) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public CertificatsAptitudeEntity updateCertificatAptitude(CertificatsAptitudeEntity CertificatAptitude) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<CertificatsAptitudeEntity> getAllCertificatAptitudeByUserId() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<CertificatsAptitudeEntity> getAllCertificatAptitudeList() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void deleteCertificatAptitudeById1(long idCertificatsaptitude) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public List<CertificatsAptitudeEntity> getAllCertificatAptitudeByUserIdWaiting() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<CertificatsAptitudeEntity> getAllCertificatAptitudeByUserIdWaiting2() {
		// TODO Auto-generated method stub
		return null;
	}

}
