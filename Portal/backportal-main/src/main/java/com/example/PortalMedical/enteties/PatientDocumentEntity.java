package com.example.PortalMedical.enteties;

import java.util.Date;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;

/** A file attached to a patient's record (analysis, imaging, letter…), stored in the database. */
@Entity
@Table(name = "patient_document")
public class PatientDocumentEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;

	private long patientId;

	/** Doctor who added it (documents are private to that doctor). */
	private long medecinId;

	private String nom;

	private String typeMime;

	private long taille;

	/** analyse, imagerie, courrier, ordonnance, autre */
	private String categorie;

	private Date dateAjout;

	@Lob
	@Basic(fetch = FetchType.LAZY)
	@Column(columnDefinition = "LONGBLOB")
	@JsonIgnore
	private byte[] contenu;

	public PatientDocumentEntity() {
	}

	/** Metadata only (used by listings, which never load the file contents). */
	public PatientDocumentEntity(long id, long patientId, long medecinId, String nom, String typeMime, long taille,
			String categorie, Date dateAjout) {
		this.id = id;
		this.patientId = patientId;
		this.medecinId = medecinId;
		this.nom = nom;
		this.typeMime = typeMime;
		this.taille = taille;
		this.categorie = categorie;
		this.dateAjout = dateAjout;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public long getPatientId() {
		return patientId;
	}

	public void setPatientId(long patientId) {
		this.patientId = patientId;
	}

	public long getMedecinId() {
		return medecinId;
	}

	public void setMedecinId(long medecinId) {
		this.medecinId = medecinId;
	}

	public String getNom() {
		return nom;
	}

	public void setNom(String nom) {
		this.nom = nom;
	}

	public String getTypeMime() {
		return typeMime;
	}

	public void setTypeMime(String typeMime) {
		this.typeMime = typeMime;
	}

	public long getTaille() {
		return taille;
	}

	public void setTaille(long taille) {
		this.taille = taille;
	}

	public String getCategorie() {
		return categorie;
	}

	public void setCategorie(String categorie) {
		this.categorie = categorie;
	}

	public Date getDateAjout() {
		return dateAjout;
	}

	public void setDateAjout(Date dateAjout) {
		this.dateAjout = dateAjout;
	}

	public byte[] getContenu() {
		return contenu;
	}

	public void setContenu(byte[] contenu) {
		this.contenu = contenu;
	}
}
