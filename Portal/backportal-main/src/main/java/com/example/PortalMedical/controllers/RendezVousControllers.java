package com.example.PortalMedical.controllers;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.example.PortalMedical.DTO.UserDTO;
import com.example.PortalMedical.Repositories.RendezVousRepository;
import com.example.PortalMedical.Repositories.UserRepository;
import com.example.PortalMedical.Services.PracticeScope;
import com.example.PortalMedical.Services.RendezVousServices;
import com.example.PortalMedical.enteties.RendezVousEntity;
//import com.example.PortalMedical.enteties.RendezVousEntity.Days;
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



	@Autowired
	PracticeScope practiceScope;

	/** Staff add an appointment, only for a doctor of their own practice. */
	@PostMapping("/ajouterApt")
	@ResponseBody
	public RendezVousEntity ajouterApt(@RequestBody RendezVousEntity apt) {
		practiceScope.requireDoctor(apt.getUser() == null ? null : apt.getUser().getId());
		return rendezVousServices.ajouterApt(apt);
	}

	@PostMapping("/demandeRDV")
	public Map<String, Object> DemandeRDV(@RequestBody RendezVousEntity apt) {
		// Public endpoint: answer with the request id only, not the doctor's full profile.
		return Collections.singletonMap("idapt", rendezVousServices.demandeRDV(apt).getIdapt());
	}

	/**
	 * Public: times (HH:mm) already taken for a doctor on a day, so the request page can
	 * grey them out. Returns no patient data.
	 */
	@GetMapping("/creneauxPris/{medecinId}")
	public List<String> creneauxPris(@PathVariable long medecinId,
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date date) {
		return rendezVousServices.takenSlots(medecinId, date);
	}

	/** Pending requests of the signed-in doctor, or of the doctor a secretary works for. */
	@GetMapping("/demandes")
	public List<RendezVousEntity> demandes() {
		return rendezVousServices.pendingRequestsFor(practiceScope.currentUser());
	}

	/** Confirmed appointments for the calendar, in the same scope as /demandes. */
	@GetMapping("/agenda")
	public List<RendezVousEntity> agenda() {
		return rendezVousServices.agendaFor(practiceScope.currentUser());
	}

	@GetMapping("/getByState/{state}")
	public List<RendezVousEntity> GetBystate(@PathVariable(name="state") String state){
		StateApt parsed;
		try {
			parsed = StateApt.valueOf(state);
		} catch (IllegalArgumentException e) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown state");
		}
		return rendezVousServices.appointmentsFor(practiceScope.currentUser(), parsed);
	}

	@GetMapping("/getById/{id}")
	public RendezVousEntity GetByid(@PathVariable(name="id") Long id){
		return requireInPractice(id);
	}

	@PutMapping("/confirmer")
	public RendezVousEntity confirmer(@RequestBody RendezVousEntity rdv) {
		requireInPractice(rdv.getIdapt());
		return rendezVousServices.confirmerRdv(rdv);
	}

	@PutMapping("/refuser")
	public RendezVousEntity refuser(@RequestBody RendezVousEntity rdv) {
		requireInPractice(rdv.getIdapt());
		return rendezVousServices.annulerRdv(rdv);
	}

	@GetMapping("/get")
	public List<RendezVousEntity> getListRV() {
		return rendezVousServices.appointmentsFor(practiceScope.currentUser(), null);
	}

	@DeleteMapping("/{id}")
	public void deleterdv(@PathVariable(name = "id") long id) {
		requireInPractice(id);
		rendezVousServices.deleteAptById(id);
	}

	/** Moves an appointment; both the old and the new doctor must be in the practice. */
	@PutMapping("/modifier")
	public RendezVousEntity updateApt(@RequestBody RendezVousEntity apt) {
		requireInPractice(apt.getIdapt());
		practiceScope.requireDoctor(apt.getUser() == null ? null : apt.getUser().getId());
		return rendezVousServices.updateApt(apt);
	}

	@GetMapping
	public List<RendezVousEntity> getAllrvs() {
		return rendezVousServices.appointmentsFor(practiceScope.currentUser(), null);
	}

	/** The appointment, if it belongs to the user's practice; otherwise 404. */
	private RendezVousEntity requireInPractice(Long id) {
		RendezVousEntity rdv = id == null ? null : rendezVousRepository.findById(id).orElse(null);
		if (rdv == null || rdv.getUser() == null || !practiceScope.canAccessDoctor(rdv.getUser().getId())) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND);
		}
		return rdv;
	}
}