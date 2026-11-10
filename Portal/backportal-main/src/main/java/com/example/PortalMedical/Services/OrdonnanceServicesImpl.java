package com.example.PortalMedical.Services;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.PortalMedical.Repositories.OrdonnanceRepository;
import com.example.PortalMedical.enteties.OrdonnanceEntity;
@Service
@Transactional
public class OrdonnanceServicesImpl implements OrdonnanceServices {
@Autowired
OrdonnanceRepository ordonnance;
	@Override
	public OrdonnanceEntity ajouterOrdonnance(OrdonnanceEntity ordonnance) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<OrdonnanceEntity> retrieveAllOrdonnance() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void deleteOrdonnanceById(long idOrdonnance) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public OrdonnanceEntity updateOrdonnance(OrdonnanceEntity ordonnance) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<OrdonnanceEntity> getAllOrdonnanceByUserId() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<OrdonnanceEntity> getAllOrdonnanceList() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void deleteOrdonnanceById1(long idOrdonnance) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public List<OrdonnanceEntity> getAllficheByUserIdWaiting() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<OrdonnanceEntity> getAllOrdonnanceByUserIdWaiting2() {
		// TODO Auto-generated method stub
		return null;
	}


}
