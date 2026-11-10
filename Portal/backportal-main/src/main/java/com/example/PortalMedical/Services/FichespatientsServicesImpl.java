package com.example.PortalMedical.Services;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.PortalMedical.Repositories.FichespatientRepository;
import com.example.PortalMedical.enteties.FichepatientEntity;

@Service
@Transactional
public class FichespatientsServicesImpl implements FichespatientsServices{
@Autowired
FichespatientRepository fiche;
	@Override
	public FichepatientEntity ajouterfiche(FichepatientEntity fiche) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public List<FichepatientEntity> retrieveAllfiche() {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public void deleteficheById(long IdFiche) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public FichepatientEntity updatefiche(FichepatientEntity fiche) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public List<FichepatientEntity> getAllficheByUserId() {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public List<FichepatientEntity> getAllficheList() {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public void deleteficheById1(long IdFiche) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public List<FichepatientEntity> getAllficheByUserIdWaiting() {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public List<FichepatientEntity> getAllficheByUserIdWaiting2() {
		// TODO Auto-generated method stub
		return null;
	}
}
