package com.example.PortalMedical.Services;

import java.util.List;

import com.example.PortalMedical.enteties.FichepatientEntity;


public interface FichespatientsServices {
		 public FichepatientEntity ajouterfiche(FichepatientEntity fiche);
		 List<FichepatientEntity> retrieveAllfiche();
		 public void deleteficheById(long IdFiche);
		 public FichepatientEntity updatefiche(FichepatientEntity fiche);
		 public List<FichepatientEntity> getAllficheByUserId() ;
		 public List<FichepatientEntity> getAllficheList();
		 public void deleteficheById1(long IdFiche);
		 public List<FichepatientEntity> getAllficheByUserIdWaiting();
		 public List<FichepatientEntity> getAllficheByUserIdWaiting2();
	}




