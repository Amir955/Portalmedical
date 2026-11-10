package com.example.PortalMedical.Services;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.PortalMedical.Repositories.CertificatsMedicalRepository;
import com.example.PortalMedical.enteties.CertificatsMedicalEntity;
@Service
@Transactional
public class CertificatsmedicalServicesImpl implements CertificatsmedicalServices{
@Autowired
CertificatsMedicalRepository certificat;
	@Override
	public CertificatsMedicalEntity ajouterCertificat(CertificatsMedicalEntity Certificat) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<CertificatsMedicalEntity> retrieveAllCertificat() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void deleteCertificatById(long idCertificats) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public CertificatsMedicalEntity updateCertificat(CertificatsMedicalEntity Certificat) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<CertificatsMedicalEntity> getAllCertificatByUserId() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<CertificatsMedicalEntity> getAllCertificatList() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void deleteCertificatById1(long idCertificats) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public List<CertificatsMedicalEntity> getAllCertificatByUserIdWaiting() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<CertificatsMedicalEntity> getAllCertificatByUserIdWaiting2() {
		// TODO Auto-generated method stub
		return null;
	}

}
