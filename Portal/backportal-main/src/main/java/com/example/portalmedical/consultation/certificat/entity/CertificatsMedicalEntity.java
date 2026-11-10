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
@Table(name="Certificats" )
public class CertificatsMedicalEntity  implements Serializable{
	private static final long serialVersionUID = 1L;
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long idCertificats;
	private Date datedecreation;
	private int nombre_de_jour_repos;
	private Date daterepos;
	@Lob
	private String commantaire;
	
	@ManyToOne
	private UserEntity user;
	
	@ManyToOne
	private PatientEntity patient;
	
	/*
	public long getIdCertificats() {
		return idCertificats;
	}
	public void setIdCertificats(long idCertificats) {
		this.idCertificats = idCertificats;
	}
	
	public Date getDatedecreation() {
		return datedecreation;
	}
	public void setDatedecreation(Date datedecreation) {
		this.datedecreation = datedecreation;
	}
	public int getNombre_de_jour_repos() {
		return nombre_de_jour_repos;
	}
	public void setNombre_de_jour_repos(int nombre_de_jour_repos) {
		this.nombre_de_jour_repos = nombre_de_jour_repos;
	}
	public Date getDaterepos() {
		return daterepos;
	}
	public void setDaterepos(Date daterepos) {
		this.daterepos = daterepos;
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
	
	public CertificatsMedicalEntity(long idCertificats, Date datedecreation, int nombre_de_jour_repos, Date daterepos,
			String commantaire) {
		super();
		this.idCertificats = idCertificats;
		this.datedecreation = datedecreation;
		this.nombre_de_jour_repos = nombre_de_jour_repos;
		this.daterepos = daterepos;
		this.commantaire = commantaire;
	}
	public CertificatsMedicalEntity() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	*/

}
