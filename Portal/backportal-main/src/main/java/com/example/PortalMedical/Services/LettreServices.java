package com.example.PortalMedical.Services;

import java.util.List;

import com.example.PortalMedical.enteties.LettreConfrereEntity;

public interface LettreServices {
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
