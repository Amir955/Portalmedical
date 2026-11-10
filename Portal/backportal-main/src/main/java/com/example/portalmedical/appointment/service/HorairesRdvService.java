package com.example.portalmedical.appointment.service;

import java.util.List;

import com.example.portalmedical.appointment.entity.HorairesRdvEntity;
import com.example.portalmedical.appointment.entity.RendezVousEntity.StateApt;

public interface HorairesRdvService {

		//public RendezVousEntity ajouterApt(RendezVousEntity apt);
		 //public void ajouterApt(RendezVousEntity apt);
		 public HorairesRdvEntity ajouterhoraire(HorairesRdvEntity horaire);
		 List<HorairesRdvEntity> retrieveAllhoraire();
		 public void deletehoraireById(long idhoraires);
		 public HorairesRdvEntity updateApt(HorairesRdvEntity horaire);
		 public List<HorairesRdvEntity> getAllRdvByUserId() ;
		 //public RendezVousEntity getAptByState(StateApt state);
		 public List<HorairesRdvEntity> getAptByState(StateApt state);
		// public List<RendezVousEntity> getAptOrderedByDate();
		 public List<HorairesRdvEntity> getAllhoraireList();
		 public void ChangeStateById(Long idhoraires);
		 public void ChangeStateById1(Long idhoraires);
		 public List<HorairesRdvEntity> AfficherDemandes();
		 public void deletehoraireById1(Long idhoraires);
		 public List<HorairesRdvEntity> getAllhoraireByUserIdWaiting();
		 public List<HorairesRdvEntity> getAllhoraireByUserIdWaiting2();
	}


