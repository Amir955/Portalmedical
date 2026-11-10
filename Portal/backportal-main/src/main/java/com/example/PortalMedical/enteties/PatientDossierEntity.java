package com.example.PortalMedical.enteties;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;

/** Medical summary of a patient (doctors only): blood group, allergies, history, treatments, notes. */
@Entity
@Table(name = "patient_dossier")
public class PatientDossierEntity {

	@Id
	private long patientId;

	private String groupeSanguin;

	@Lob
	@Column(columnDefinition = "LONGTEXT")
	private String allergies;

	@Lob
	@Column(columnDefinition = "LONGTEXT")
	private String antecedents;

	@Lob
	@Column(columnDefinition = "LONGTEXT")
	private String traitements;

	/** Private notes of the doctor. */
	@Lob
	@Column(columnDefinition = "LONGTEXT")
	private String notes;

	private Date dateMaj;

	private String majPar;

	public long getPatientId() {
		return patientId;
	}

	public void setPatientId(long patientId) {
		this.patientId = patientId;
	}

	public String getGroupeSanguin() {
		return groupeSanguin;
	}

	public void setGroupeSanguin(String groupeSanguin) {
		this.groupeSanguin = groupeSanguin;
	}

	public String getAllergies() {
		return allergies;
	}

	public void setAllergies(String allergies) {
		this.allergies = allergies;
	}

	public String getAntecedents() {
		return antecedents;
	}

	public void setAntecedents(String antecedents) {
		this.antecedents = antecedents;
	}

	public String getTraitements() {
		return traitements;
	}

	public void setTraitements(String traitements) {
		this.traitements = traitements;
	}

	public String getNotes() {
		return notes;
	}

	public void setNotes(String notes) {
		this.notes = notes;
	}

	public Date getDateMaj() {
		return dateMaj;
	}

	public void setDateMaj(Date dateMaj) {
		this.dateMaj = dateMaj;
	}

	public String getMajPar() {
		return majPar;
	}

	public void setMajPar(String majPar) {
		this.majPar = majPar;
	}
}
