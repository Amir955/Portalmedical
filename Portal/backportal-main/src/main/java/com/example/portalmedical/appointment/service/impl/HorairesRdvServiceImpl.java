package com.example.portalmedical.appointment.service.impl;


import com.example.portalmedical.appointment.service.HorairesRdvService;
import java.util.List;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.portalmedical.appointment.repository.HorairesRdvRepository;
import com.example.portalmedical.appointment.repository.RendezVousRepository;
import com.example.portalmedical.appointment.entity.HorairesRdvEntity;
import com.example.portalmedical.appointment.entity.RendezVousEntity.StateApt;

@Service
@Transactional
public class HorairesRdvServiceImpl implements HorairesRdvService{
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
