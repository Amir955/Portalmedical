package com.example.PortalMedical.enteties;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "users")
public class UserEntity {
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
    private Sexe sexe;
    private String pays;
    @Enumerated(EnumType.STRING)
    private Gouvernerats gouvernerat;
    private int codepostal;
    private String ville;
    @Column(nullable = true)
    private String password;
    private Date creationDate;
    private int numtelephone;

     

	
	public int getNumtelephone() {
		return numtelephone;
	}
	public void setNumtelephone(int numtelephone) {
		this.numtelephone = numtelephone;
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
	public Sexe getSexe() {
		return sexe;
	}
	public void setSexe(Sexe sexe) {
		this.sexe = sexe;
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
    
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public Role getRole() {
		return role;
	}
	public void setRole(Role role) {
		this.role = role;
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
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
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


public UserEntity(String password, String email) {
    this.password = password;
    this.email = email;
}

public UserEntity() {
		super();

	}
public UserEntity(long id, String nom, String prenom, Date age, String cin, Role role, String email, Sexe sexe,
		String pays, Gouvernerats gouvernerat, int codepostal, String ville, String password, Date creationDate,
		int numtelephone) {
	super();
	this.id = id;
	this.nom = nom;
	this.prenom = prenom;
	this.age = age;
	this.cin = cin;
	this.role = role;
	this.email = email;
	this.sexe = sexe;
	this.pays = pays;
	this.gouvernerat = gouvernerat;
	this.codepostal = codepostal;
	this.ville = ville;
	this.password = password;
	this.creationDate = creationDate;
	this.numtelephone = numtelephone;
}




}



    

