package com.example.portalmedical.consultation.lettre.service;

import java.util.List;

import com.example.portalmedical.consultation.lettre.entity.LettreConfrereEntity;

public interface LettreService {
	public LettreConfrereEntity ajouterletter(LettreConfrereEntity letter);
	 List<LettreConfrereEntity> retrieveAllletter();
	 public void deleteletterById(long idlettre);
	 public LettreConfrereEntity updateletter(LettreConfrereEntity letter);
	 public List<LettreConfrereEntity> getAllletterByUserId() ;
	 public List<LettreConfrereEntity> getAllletterList();
	 public void deleteletterById1(long idlettre);
	 public List<LettreConfrereEntity> getAllletterByUserIdWaiting();
	 public List<LettreConfrereEntity> getAllletterByUserIdWaiting2();
}
