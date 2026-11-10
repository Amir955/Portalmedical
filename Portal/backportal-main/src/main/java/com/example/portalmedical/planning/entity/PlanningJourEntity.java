package com.example.portalmedical.planning.entity;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/** Opening hours of one doctor for one weekday (1 = Monday … 7 = Sunday). Times are "HH:mm". */
@Entity
@Table(name = "planning_jour")
public class PlanningJourEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;

	private long medecinId;

	private int jour;

	private boolean actif;

	private String debutMatin;

	private String finMatin;

	private String debutApresMidi;

	private String finApresMidi;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public long getMedecinId() {
		return medecinId;
	}

	public void setMedecinId(long medecinId) {
		this.medecinId = medecinId;
	}

	public int getJour() {
		return jour;
	}

	public void setJour(int jour) {
		this.jour = jour;
	}

	public boolean isActif() {
		return actif;
	}

	public void setActif(boolean actif) {
		this.actif = actif;
	}

	public String getDebutMatin() {
		return debutMatin;
	}

	public void setDebutMatin(String debutMatin) {
		this.debutMatin = debutMatin;
	}

	public String getFinMatin() {
		return finMatin;
	}

	public void setFinMatin(String finMatin) {
		this.finMatin = finMatin;
	}

	public String getDebutApresMidi() {
		return debutApresMidi;
	}

	public void setDebutApresMidi(String debutApresMidi) {
		this.debutApresMidi = debutApresMidi;
	}

	public String getFinApresMidi() {
		return finApresMidi;
	}

	public void setFinApresMidi(String finApresMidi) {
		this.finApresMidi = finApresMidi;
	}
}
