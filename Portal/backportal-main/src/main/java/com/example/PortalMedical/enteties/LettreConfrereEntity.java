package com.example.PortalMedical.enteties;

import java.io.Serializable;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Entity
@Table (name="lettre")
public class LettreConfrereEntity implements Serializable{
	private static final long serialVersionUID = 1L;
	@Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long idlettre;
	private Date datedecreation;
	private String nondeconfrere;
	private String Prenomdeconfrere;
	private String Specialitedeconfrere;
	private String Adressedeconfere;
	private int Telephoneconfrere;
	private int GSMConfrere;
	private String Etatdesantedupatient;
	
	@ManyToOne
	private PatientEntity patient;
	
	@ManyToOne
	private UserEntity user;

	public long getIdlettre() {
		return idlettre;
	}
	public void setIdlettre(long idlettre) {
		this.idlettre = idlettre;
	}
	public Date getDatedecreation() {
		return datedecreation;
	}
	public void setDatedecreation(Date datedecreation) {
		this.datedecreation = datedecreation;
	}
	public String getNondeconfrere() {
		return nondeconfrere;
	}
	public void setNondeconfrere(String nondeconfrere) {
		this.nondeconfrere = nondeconfrere;
	}
	public String getPrenomdeconfrere() {
		return Prenomdeconfrere;
	}
	public void setPrenomdeconfrere(String prenomdeconfrere) {
		Prenomdeconfrere = prenomdeconfrere;
	}
	public String getSpecialitedeconfrere() {
		return Specialitedeconfrere;
	}
	public void setSpecialitedeconfrere(String specialitedeconfrere) {
		Specialitedeconfrere = specialitedeconfrere;
	}
	public String getAdressedeconfere() {
		return Adressedeconfere;
	}
	public void setAdressedeconfere(String adressedeconfere) {
		Adressedeconfere = adressedeconfere;
	}
	public int getTelephoneconfrere() {
		return Telephoneconfrere;
	}
	public void setTelephoneconfrere(int telephoneconfrere) {
		Telephoneconfrere = telephoneconfrere;
	}
	public int getGSMConfrere() {
		return GSMConfrere;
	}
	public void setGSMConfrere(int gSMConfrere) {
		GSMConfrere = gSMConfrere;
	}
	public String getEtatdesantedupatient() {
		return Etatdesantedupatient;
	}
	public void setEtatdesantedupatient(String etatdesantedupatient) {
		Etatdesantedupatient = etatdesantedupatient;
	}
	public PatientEntity getPatient() {
		return patient;
	}
	public void setPatient(PatientEntity patient) {
		this.patient = patient;
	}
	public UserEntity getUser() {
		return user;
	}
	public void setUser(UserEntity user) {
		this.user = user;
	}
	public static long getSerialversionuid() {
		return serialVersionUID;
	}
	public LettreConfrereEntity(long idlettre, Date datedecreation, String nondeconfrere, String prenomdeconfrere,
			String specialitedeconfrere, String adressedeconfere, int telephoneconfrere, int gSMConfrere,
			String etatdesantedupatient) {
		super();
		this.idlettre = idlettre;
		this.datedecreation = datedecreation;
		this.nondeconfrere = nondeconfrere;
		Prenomdeconfrere = prenomdeconfrere;
		Specialitedeconfrere = specialitedeconfrere;
		Adressedeconfere = adressedeconfere;
		Telephoneconfrere = telephoneconfrere;
		GSMConfrere = gSMConfrere;
		Etatdesantedupatient = etatdesantedupatient;
	}
	public LettreConfrereEntity() {
		super();
		// TODO Auto-generated constructor stub
	}

	
}
