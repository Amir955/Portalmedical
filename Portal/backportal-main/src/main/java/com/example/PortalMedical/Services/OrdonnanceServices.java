package com.example.PortalMedical.Services;

import java.util.List;

import com.example.PortalMedical.enteties.OrdonnanceEntity;


public interface OrdonnanceServices {
	 public OrdonnanceEntity ajouterOrdonnance(OrdonnanceEntity ordonnance);
	 List<OrdonnanceEntity> retrieveAllOrdonnance();
	 public void deleteOrdonnanceById(long idOrdonnance);
	 public OrdonnanceEntity updateOrdonnance(OrdonnanceEntity ordonnance);
	 public List<OrdonnanceEntity> getAllOrdonnanceByUserId() ;
	 public List<OrdonnanceEntity> getAllOrdonnanceList();
	 public void deleteOrdonnanceById1(long idOrdonnance);
	 public List<OrdonnanceEntity> getAllficheByUserIdWaiting();
	 public List<OrdonnanceEntity> getAllOrdonnanceByUserIdWaiting2();
}
