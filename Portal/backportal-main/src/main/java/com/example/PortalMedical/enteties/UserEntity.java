package com.example.PortalMedical.enteties;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;


import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Entity
@Table(name = "users")
public class UserEntity implements Serializable {
	private static final long serialVersionUID = 1L;
@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private String nom;
    private String prenom;
    private Date age ;
    private String cin ; 
    @Enumerated(EnumType.STRING)
    private Role role ;
    private String email;
    @Enumerated(EnumType.STRING)
    private Genre sexe;
    private String pays;
    @Enumerated(EnumType.STRING)
    private Gouvernerats gouvernerat;
    private int codepostal;
    private String ville;
    @Column(nullable = true)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;
    private Date creationDate;
    private String numtelephone;

    /** For a secretary: id of the doctor who created her account (null for other roles). */
    private Long medecinId;

    public Long getMedecinId() {
        return medecinId;
    }

    public void setMedecinId(Long medecinId) {
        this.medecinId = medecinId;
    }
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
	public Role getRole() {
		return role;
	}
	public void setRole(Role role) {
		this.role = role;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public Genre getSexe() {
		return sexe;
	}
	public void setSexe(Genre sexe) {
		this.sexe = sexe;
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
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public Date getCreationDate() {
		return creationDate;
	}
	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}
	public String getNumtelephone() {
		return numtelephone;
	}
	public void setNumtelephone(String numtelephone) {
		this.numtelephone = numtelephone;
	}
	public static long getSerialversionuid() {
		return serialVersionUID;
	}
	public UserEntity() {
		super();
		// TODO Auto-generated constructor stub
	}
   
}



    

