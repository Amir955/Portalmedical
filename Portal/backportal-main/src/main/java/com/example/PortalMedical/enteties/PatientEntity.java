package com.example.PortalMedical.enteties;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Entity
@Table(name = "patient")

public class PatientEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
	
    private String nom;
    private String prenom;
    private String email;
    private String numtelephone;
    private Date age ;
    private String cin ;
    public PatientEntity() {
		super();
	}
	private String pays;
    private Gouvernerats gouvernerat;
    private Date creationDate;
    private int codepostal;
    private String ville;
	private Genre sexe ;

    /** Doctor whose practice this patient belongs to (null for older shared records). */
    private Long medecinId;

    public Long getMedecinId() {
        return medecinId;
    }

    public void setMedecinId(Long medecinId) {
        this.medecinId = medecinId;
    }
    @OneToMany(mappedBy = "patient")
   @JsonIgnore
   private List<LettreConfrereEntity> lettres ;
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public String getNom() {
		return nom;
	}
	public void setNom(String nom) {
		this.nom = nom;
	}
	public String getPrenom() {
		return prenom;
	}
	public void setPrenom(String prenom) {
		this.prenom = prenom;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getNumtelephone() {
		return numtelephone;
	}
	public void setNumtelephone(String numtelephone) {
		this.numtelephone = numtelephone;
	}
	public Date getAge() {
		return age;
	}
	public void setAge(Date age) {
		this.age = age;
	}
	public String getCin() {
		return cin;
	}
	public void setCin(String cin) {
		this.cin = cin;
	}
	public String getPays() {
		return pays;
	}
	public void setPays(String pays) {
		this.pays = pays;
	}
	public Gouvernerats getGouvernerat() {
		return gouvernerat;
	}
	public void setGouvernerat(Gouvernerats gouvernerat) {
		this.gouvernerat = gouvernerat;
	}
	public Date getCreationDate() {
		return creationDate;
	}
	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}
	public int getCodepostal() {
		return codepostal;
	}
	public void setCodepostal(int codepostal) {
		this.codepostal = codepostal;
	}
	public String getVille() {
		return ville;
	}
	public void setVille(String ville) {
		this.ville = ville;
	}
	public Genre getSexe() {
		return sexe;
	}
	public void setSexe(Genre sexe) {
		this.sexe = sexe;
	}
	public List<LettreConfrereEntity> getLettres() {
		return lettres;
	}
	public void setLettres(List<LettreConfrereEntity> lettres) {
		this.lettres = lettres;
	}
}
