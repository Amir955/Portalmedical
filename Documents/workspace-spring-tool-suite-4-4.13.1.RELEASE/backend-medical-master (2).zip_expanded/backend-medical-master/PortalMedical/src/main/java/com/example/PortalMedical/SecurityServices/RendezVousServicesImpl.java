package com.example.PortalMedical.SecurityServices;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.PortalMedical.Repositories.RendezVousRepository;
import com.example.PortalMedical.Repositories.UserRepository;
import com.example.PortalMedical.enteties.RendezVousEntity;
import com.example.PortalMedical.enteties.RendezVousEntity.StateApt;

@Service
@Transactional
public class RendezVousServicesImpl implements RendezVousServices {

	@Autowired
    RendezVousRepository rvrepo;
	@Autowired
    UserRepository userRepository;
	
	@Override
	public RendezVousEntity ajouterApt(RendezVousEntity apt) {
		return rvrepo.save(apt);
	}

	@Override
	public List<RendezVousEntity> retrieveAllApt() {
		return (List<RendezVousEntity>) rvrepo.findAll();

	}

	@Override
	public void deleteAptById(long aptId) {
		rvrepo.deleteById(aptId);			
	}

	@Override
	public RendezVousEntity updateApt(RendezVousEntity apt) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<RendezVousEntity> getAllRdvByUserId() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<RendezVousEntity> getAptByState(StateApt state) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<RendezVousEntity> getAllAptList() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void ChangeStateById(int aptId) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void ChangeStateById1(int aptId) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public List<RendezVousEntity> AfficherDemandes() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void deleteAdsById1(int aptId) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public List<RendezVousEntity> getAllRdvByUserIdWaiting() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<RendezVousEntity> getTodaysApt() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<RendezVousEntity> getAllRdvByUserIdWaiting2() {
		// TODO Auto-generated method stub
		return null;
	} 	

}