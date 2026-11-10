package com.example.portalmedical.consultation.ordonnance.service;

import java.util.List;

import com.example.portalmedical.consultation.ordonnance.entity.OrdonnanceEntity;


public interface OrdonnanceService {
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
