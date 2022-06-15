package com.example.PortalMedical.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.example.PortalMedical.Repositories.RendezVousRepository;
import com.example.PortalMedical.Repositories.UserRepository;
import com.example.PortalMedical.SecurityServices.RendezVousServices;
import com.example.PortalMedical.enteties.RendezVousEntity;
import com.example.PortalMedical.enteties.RendezVousEntity.Days;
import com.example.PortalMedical.enteties.RendezVousEntity.StateApt;
import com.example.PortalMedical.enteties.UserEntity;
@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/rendezvous")

public class RendezVousControllers {
	@SuppressWarnings("unused")
	@Autowired
	private JavaMailSender javaMailSender;
	
	@Autowired
	RendezVousServices rendezVousServices;
	@Autowired
	UserRepository userRepository;
	SimpleMailMessage msg = new SimpleMailMessage();

	@Autowired
	RendezVousRepository rendezVousRepository;
	
	StateApt state1 = StateApt.waiting ;
	StateApt state2 = StateApt.confirmed ;
	StateApt state3 = StateApt.demande ;

	Days jour;


	@PostMapping("/ajouterApt")
	@ResponseBody
	public RendezVousEntity ajouterApt(@RequestBody RendezVousEntity apt) {
		UserEntity user = userRepository.findById(apt.getUser().getId()).get();
		apt.setUser(user);
		RendezVousEntity R= rendezVousServices.ajouterApt(apt);
		return R;
	}
	
	@GetMapping("/get")
	public List<RendezVousEntity> getListRV() {
		return rendezVousServices.retrieveAllApt();
	}
	@DeleteMapping("/{id}")
	public void deleterdv(@PathVariable(name = "id") long id) {
		rendezVousServices.deleteAptById(id);
}
	@PutMapping
	public RendezVousEntity updateApt(@RequestBody RendezVousEntity apt) {
		return rendezVousServices.updateApt(apt);
	}
	}