package com.example.PortalMedical.enteties;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import com.fasterxml.jackson.annotation.JsonIgnore;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import com.sun.istack.Nullable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;



@Data
@Entity
@Table(name = "Rendezvous")
public class RendezVousEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	
	@Id
	@GeneratedValue (strategy = GenerationType.IDENTITY)
	private long idapt;
	
	private Date dateOfApt;
	
	private String patientEmail;
	
	@Enumerated(EnumType.STRING)
	private StateApt stateapt;
	public enum StateApt {
		   waiting , confirmed , refused , demande ,
		   /** Cancelled by the patient from the link in their e-mail. */
		   cancelled
		}
	
	@OneToOne
	private UserEntity user;
	
	
	@OneToOne
	private PatientEntity patient;

	// Patient details typed on the public request form. The patient record is
	// created (or found by e-mail) only when the request is confirmed.
	private String patientNom;

	private String patientPrenom;

	private String patientTelephone;

	@Temporal(TemporalType.DATE)
	private Date patientDateNaissance;

	/** Private code in the patient's "Annuler mon rendez-vous" link (never exposed in lists). */
	@Column(unique = true, length = 64)
	@JsonIgnore
	private String jetonAnnulation;

	/** True once the reminder of the day before has been sent. */
	@JsonIgnore
	private Boolean rappelEnvoye;

	/** Day-of-visit status: null (planned), arrive, en_consultation, termine, absent. */
	private String statutVisite;

	/** When the patient was marked as arrived (waiting room). */
	private Date heureArrivee;

	public String getStatutVisite() {
		return statutVisite;
	}

	public void setStatutVisite(String statutVisite) {
		this.statutVisite = statutVisite;
	}

	public Date getHeureArrivee() {
		return heureArrivee;
	}

	public void setHeureArrivee(Date heureArrivee) {
		this.heureArrivee = heureArrivee;
	}

	public String getJetonAnnulation() {
		return jetonAnnulation;
	}

	public void setJetonAnnulation(String jetonAnnulation) {
		this.jetonAnnulation = jetonAnnulation;
	}

	public Boolean getRappelEnvoye() {
		return rappelEnvoye;
	}

	public void setRappelEnvoye(Boolean rappelEnvoye) {
		this.rappelEnvoye = rappelEnvoye;
	}

	public String getPatientNom() {
		return patientNom;
	}

	public void setPatientNom(String patientNom) {
		this.patientNom = patientNom;
	}

	public String getPatientPrenom() {
		return patientPrenom;
	}

	public void setPatientPrenom(String patientPrenom) {
		this.patientPrenom = patientPrenom;
	}

	public String getPatientTelephone() {
		return patientTelephone;
	}

	public void setPatientTelephone(String patientTelephone) {
		this.patientTelephone = patientTelephone;
	}

	public Date getPatientDateNaissance() {
		return patientDateNaissance;
	}

	public void setPatientDateNaissance(Date patientDateNaissance) {
		this.patientDateNaissance = patientDateNaissance;
	}


	public long getIdapt() {
		return idapt;
	}


	public void setIdapt(long idapt) {
		this.idapt = idapt;
	}


	public Date getDateOfApt() {
		return dateOfApt;
	}


	public void setDateOfApt(Date dateOfApt) {
		this.dateOfApt = dateOfApt;
	}


	public String getPatientEmail() {
		return patientEmail;
	}


	public void setPatientEmail(String patientEmail) {
		this.patientEmail = patientEmail;
	}


	public StateApt getStateapt() {
		return stateapt;
	}


	public void setStateapt(StateApt stateapt) {
		this.stateapt = stateapt;
	}


	public UserEntity getUser() {
		return user;
	}


	public void setUser(UserEntity user) {
		this.user = user;
	}


	public PatientEntity getPatient() {
		return patient;
	}


	public void setPatient(PatientEntity patient) {
		this.patient = patient;
	}


	public static long getSerialversionuid() {
		return serialVersionUID;
	}


	public RendezVousEntity() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	
	




}