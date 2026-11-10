package com.example.portalmedical.consultation.certificat.entity;


import com.example.portalmedical.patient.entity.PatientEntity;
import com.example.portalmedical.user.entity.UserEntity;
import java.io.Serializable;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table (name="certificatsaptitude")
public class CertificatsAptitudeEntity implements Serializable{
	private static final long serialVersionUID = 1L;
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long idCertificatsaptitude;
	private Date datedecreation;
	private String profession;
	private String Etat_de_sante;
	@Lob
	private String commantaire;
	
	@ManyToOne
	private UserEntity user;
	
	@ManyToOne
	private PatientEntity patient;
	
	/*
	public long getIdCertificatsaptitude() {
		return idCertificatsaptitude;
	}
	public void setIdCertificatsaptitude(long idCertificatsaptitude) {
		this.idCertificatsaptitude = idCertificatsaptitude;
	}
	
	public Date getDatedecreation() {
		return datedecreation;
	}
	public void setDatedecreation(Date datedecreation) {
		this.datedecreation = datedecreation;
	}
	public String getProfession() {
		return profession;
	}
	public void setProfession(String profession) {
		this.profession = profession;
	}
	public String getEtat_de_sante() {
		return Etat_de_sante;
	}
	public void setEtat_de_sante(String etat_de_sante) {
		Etat_de_sante = etat_de_sante;
	}
	public String getCommantaire() {
		return commantaire;
	}
	public void setCommantaire(String commantaire) {
		this.commantaire = commantaire;
	}
	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public CertificatsAptitudeEntity(long idCertificatsaptitude, Date datedecreation, String profession,
			String etat_de_sante, String commantaire) {
		super();
		this.idCertificatsaptitude = idCertificatsaptitude;
		this.datedecreation = datedecreation;
		this.profession = profession;
		Etat_de_sante = etat_de_sante;
		this.commantaire = commantaire;
	}
	public CertificatsAptitudeEntity() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	*/
}
