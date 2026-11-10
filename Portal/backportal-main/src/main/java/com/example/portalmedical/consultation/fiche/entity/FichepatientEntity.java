package com.example.portalmedical.consultation.fiche.entity;


import com.example.portalmedical.patient.entity.PatientEntity;
import com.example.portalmedical.user.entity.UserEntity;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name="FichePatient")
public class FichepatientEntity {
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
	private long IdFiche;
	private Date Datedecreation;
	private String Matriculecnam;
	@Lob
	@Column(columnDefinition = "LONGTEXT")
	private String Causedeconsultation;
	@Lob
	@Column(columnDefinition = "LONGTEXT")
	private String Examen;
	@Lob
	@Column(columnDefinition = "LONGTEXT")
	private String Suivie;
	
	@ManyToOne
	private PatientEntity patient;
	@ManyToOne
	private UserEntity user;
	
	public Long getIdFiche() {
		return IdFiche;
	}
	public PatientEntity getPatient() {
		return patient;
	}
	public void setPatient(PatientEntity patient) {
		this.patient = patient;
	}
	public Date getDatedecreation() {
		return Datedecreation;
	}
	public void setDatedecreation(Date datedecreation) {
		Datedecreation = datedecreation;
	}
	public String getMatriculecnam() {
		return Matriculecnam;
	}
	public void setMatriculecnam(String matriculecnam) {
		Matriculecnam = matriculecnam;
	}
	public String getCausedeconsultation() {
		return Causedeconsultation;
	}
	public void setCausedeconsultation(String causedeconsultation) {
		Causedeconsultation = causedeconsultation;
	}
	public String getExamen() {
		return Examen;
	}
	public void setExamen(String examen) {
		Examen = examen;
	}
	public String getSuivie() {
		return Suivie;
	}
	public void setSuivie(String suivie) {
		Suivie = suivie;
	}
	public void setIdFiche(long idFiche) {
		IdFiche = idFiche;
	}
	public UserEntity getUser() {
		return user;
	}
	public void setUser(UserEntity user) {
		this.user = user;
	}
	public FichepatientEntity() {
		super();
		// TODO Auto-generated constructor stub
	}
	public FichepatientEntity(long idFiche, Date datedecreation, String matriculecnam, String causedeconsultation,
			String examen, String suivie) {
		super();
		this.IdFiche = idFiche;
		this.Datedecreation = datedecreation;
		this.Matriculecnam = matriculecnam;
		this.Causedeconsultation = causedeconsultation;
		this.Examen = examen;
		this.Suivie = suivie;
	}
	
}
