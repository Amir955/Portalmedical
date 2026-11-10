package com.example.portalmedical.consultation.certificat.service.impl;


import com.example.portalmedical.consultation.certificat.service.CertificatMedicalService;
import java.util.List;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.portalmedical.consultation.certificat.repository.CertificatMedicalRepository;
import com.example.portalmedical.consultation.certificat.entity.CertificatsMedicalEntity;
@Service
@Transactional
public class CertificatMedicalServiceImpl implements CertificatMedicalService{
@Autowired
CertificatMedicalRepository certificat;
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
