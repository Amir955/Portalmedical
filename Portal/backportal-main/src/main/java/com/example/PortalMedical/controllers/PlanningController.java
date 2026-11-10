package com.example.PortalMedical.controllers;

import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.example.PortalMedical.Services.PlanningService;
import com.example.PortalMedical.Services.PracticeScope;
import com.example.PortalMedical.enteties.CongeEntity;

/** Opening hours and days off of the signed-in user's practice, and public bookable slots. */
@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/planning")
public class PlanningController {

	@Autowired
	private PlanningService planningService;

	@Autowired
	private PracticeScope practiceScope;

	/** Public: bookable times of a doctor on a day (the request page and the calendar). */
	@GetMapping("/{medecinId}/creneaux")
	public PlanningService.Creneaux creneaux(@PathVariable long medecinId,
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
		return planningService.creneaux(medecinId, date);
	}

	@GetMapping("/mien")
	public PlanningService.Planning mine() {
		return planningService.get(requireDoctorId());
	}

	@PutMapping("/mien")
	public PlanningService.Planning save(@RequestBody PlanningService.Planning planning) {
		return planningService.save(requireDoctorId(), planning);
	}

	@PostMapping("/mien/conges")
	public CongeEntity addConge(@RequestBody CongeEntity conge) {
		return planningService.addConge(requireDoctorId(), conge);
	}

	@DeleteMapping("/mien/conges/{id}")
	public void deleteConge(@PathVariable long id) {
		planningService.deleteConge(requireDoctorId(), id);
	}

	/** The doctor of the signed-in user's practice (a secretary manages her doctor's hours). */
	private long requireDoctorId() {
		Long doctorId = practiceScope.currentDoctorId();
		if (doctorId == null) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No doctor linked to this account");
		}
		return doctorId;
	}
}
