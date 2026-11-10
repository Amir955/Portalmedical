package com.example.PortalMedical.enteties;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/** Per-doctor planning settings (slot length in minutes). */
@Entity
@Table(name = "planning_parametres")
public class PlanningParametresEntity {

	@Id
	private long medecinId;

	private int dureeCreneau;

	public long getMedecinId() {
		return medecinId;
	}

	public void setMedecinId(long medecinId) {
		this.medecinId = medecinId;
	}

	public int getDureeCreneau() {
		return dureeCreneau;
	}

	public void setDureeCreneau(int dureeCreneau) {
		this.dureeCreneau = dureeCreneau;
	}
}
