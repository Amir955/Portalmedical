package com.example.PortalMedical.Services;

import java.util.List;

import com.example.PortalMedical.enteties.HorairesRdvEntity;
import com.example.PortalMedical.enteties.RendezVousEntity.StateApt;

public interface HorairesRdvServices {

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


