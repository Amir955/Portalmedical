package com.example.portalmedical.payment.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/** A payment received for a consultation (amount in dinars, 3 decimals). */
@Entity
@Table(name = "paiement")
public class PaiementEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;

	/** The appointment paid for (one payment per appointment), or null. */
	@Column(unique = true)
	private Long rendezVousId;

	private Long patientId;

	private long medecinId;

	/** Patient's name at the time of payment (kept even if the record changes). */
	private String patientNom;

	private double montant;

	/** especes, cheque, carte, virement */
	private String mode;

	private Date datePaiement;

	private String note;

	/** Who recorded it (doctor or secretary). */
	private String encaissePar;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public Long getRendezVousId() {
		return rendezVousId;
	}

	public void setRendezVousId(Long rendezVousId) {
		this.rendezVousId = rendezVousId;
	}

	public Long getPatientId() {
		return patientId;
	}

	public void setPatientId(Long patientId) {
		this.patientId = patientId;
	}

	public long getMedecinId() {
		return medecinId;
	}

	public void setMedecinId(long medecinId) {
		this.medecinId = medecinId;
	}

	public String getPatientNom() {
		return patientNom;
	}

	public void setPatientNom(String patientNom) {
		this.patientNom = patientNom;
	}

	public double getMontant() {
		return montant;
	}

	public void setMontant(double montant) {
		this.montant = montant;
	}

	public String getMode() {
		return mode;
	}

	public void setMode(String mode) {
		this.mode = mode;
	}

	public Date getDatePaiement() {
		return datePaiement;
	}

	public void setDatePaiement(Date datePaiement) {
		this.datePaiement = datePaiement;
	}

	public String getNote() {
		return note;
	}

	public void setNote(String note) {
		this.note = note;
	}

	public String getEncaissePar() {
		return encaissePar;
	}

	public void setEncaissePar(String encaissePar) {
		this.encaissePar = encaissePar;
	}

	/** Receipt number shown on the PDF, e.g. R-2026-00042. */
	public String getNumeroRecu() {
		Date date = datePaiement == null ? new Date() : datePaiement;
		java.util.Calendar cal = java.util.Calendar.getInstance();
		cal.setTime(date);
		return String.format("R-%d-%05d", cal.get(java.util.Calendar.YEAR), id);
	}
}
