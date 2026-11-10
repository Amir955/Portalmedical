package com.example.PortalMedical.Services;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.PortalMedical.Repositories.HorairesRdvRepository;
import com.example.PortalMedical.Repositories.RendezVousRepository;
import com.example.PortalMedical.enteties.HorairesRdvEntity;
import com.example.PortalMedical.enteties.RendezVousEntity.StateApt;

@Service
@Transactional
public class HorairesRdvServiseImpl implements HorairesRdvServices{
@Autowired 
HorairesRdvRepository horairerdv;
@Autowired
RendezVousRepository rdv;
@Override
public HorairesRdvEntity ajouterhoraire(HorairesRdvEntity horaire) {
	// TODO Auto-generated method stub
	return null;
}
@Override
public List<HorairesRdvEntity> retrieveAllhoraire() {
	// TODO Auto-generated method stub
	return null;
}
@Override
public void deletehoraireById(long idhoraires) {
	// TODO Auto-generated method stub
	
}
@Override
public HorairesRdvEntity updateApt(HorairesRdvEntity horaire) {
	// TODO Auto-generated method stub
	return null;
}
@Override
public List<HorairesRdvEntity> getAllRdvByUserId() {
	// TODO Auto-generated method stub
	return null;
}
@Override
public List<HorairesRdvEntity> getAptByState(StateApt state) {
	// TODO Auto-generated method stub
	return null;
}
@Override
public List<HorairesRdvEntity> getAllhoraireList() {
	// TODO Auto-generated method stub
	return null;
}
@Override
public void ChangeStateById(Long idhoraires) {
	// TODO Auto-generated method stub
	
}
@Override
public void ChangeStateById1(Long idhoraires) {
	// TODO Auto-generated method stub
	
}
@Override
public List<HorairesRdvEntity> AfficherDemandes() {
	// TODO Auto-generated method stub
	return null;
}
@Override
public void deletehoraireById1(Long idhoraires) {
	// TODO Auto-generated method stub
	
}
@Override
public List<HorairesRdvEntity> getAllhoraireByUserIdWaiting() {
	// TODO Auto-generated method stub
	return null;
}
@Override
public List<HorairesRdvEntity> getAllhoraireByUserIdWaiting2() {
	// TODO Auto-generated method stub
	return null;
}

}
