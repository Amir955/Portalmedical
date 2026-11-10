package com.example.PortalMedical.Services;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.PortalMedical.Repositories.LettreRepository;
import com.example.PortalMedical.enteties.LettreConfrereEntity;
@Service
@Transactional
public class LettreServicesImpl implements LettreServices{
@Autowired
LettreRepository letter;
	@Override
	public LettreConfrereEntity ajouterletter(LettreConfrereEntity letter) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public List<LettreConfrereEntity> retrieveAllletter() {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public void deleteletterById(long idlettre) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public LettreConfrereEntity updateletter(LettreConfrereEntity letter) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public List<LettreConfrereEntity> getAllletterByUserId() {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public List<LettreConfrereEntity> getAllletterList() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void deleteletterById1(long idlettre) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public List<LettreConfrereEntity> getAllletterByUserIdWaiting() {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public List<LettreConfrereEntity> getAllletterByUserIdWaiting2() {
		// TODO Auto-generated method stub
		return null;
	}
}
