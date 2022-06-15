package com.example.PortalMedical.SecurityServices;

import java.util.List;

import com.example.PortalMedical.enteties.RendezVousEntity;
import com.example.PortalMedical.enteties.RendezVousEntity.StateApt;

public interface RendezVousServices {

	//public RendezVousEntity ajouterApt(RendezVousEntity apt);
	 //public void ajouterApt(RendezVousEntity apt);
	 public RendezVousEntity ajouterApt(RendezVousEntity apt);
	 List<RendezVousEntity> retrieveAllApt();
	 public void deleteAptById(long aptId);
	 public RendezVousEntity updateApt(RendezVousEntity apt);
	 public List<RendezVousEntity> getAllRdvByUserId() ;
	 //public RendezVousEntity getAptByState(StateApt state);
	 public List<RendezVousEntity> getAptByState(StateApt state);
	// public List<RendezVousEntity> getAptOrderedByDate();
	 public List<RendezVousEntity> getAllAptList();
	 public void ChangeStateById(int aptId);
	 public void ChangeStateById1(int aptId);
	 public List<RendezVousEntity> AfficherDemandes();
	 public void deleteAdsById1(int aptId);
	 public List<RendezVousEntity> getAllRdvByUserIdWaiting();
	 public List<RendezVousEntity> getTodaysApt();
	 public List<RendezVousEntity> getAllRdvByUserIdWaiting2();
}
