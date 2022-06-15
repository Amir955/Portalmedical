package com.example.PortalMedical.DTO;

import java.sql.Date;

import com.example.PortalMedical.enteties.Gouvernerats;
import com.example.PortalMedical.enteties.Role;
import com.example.PortalMedical.enteties.Sexe;

public class UserDTO {

    private long id;
    private String nom;
    private String prenom;
    private Date age ;
    private String cin ;
    private Role role ;
    private String email;
    private String pays;
    private Gouvernerats gouvernerat;
	private String password;
    private Date creationDate;
    private int numtelephone;

    private int codepostal;
    private String ville;
	private Sexe sexe ;

	

	public int getNumtelephone() {
		return numtelephone;
	}

	public void setNumtelephone(int numtelephone) {
		this.numtelephone = numtelephone;
	}

	public Sexe getSexe() {
		return sexe;
	}

	public void setSexe(Sexe sexe) {
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

    public Date getCreationDate() {
        return creationDate;
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

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public UserDTO(long id, String nom, String prenom, Date age, String cin, Role role, String email, String pays,
 			Gouvernerats gouvernerat, int codepostal, String ville, Sexe sexe, String password, Date creationDate) {
 		super();
 		this.id = id;
 		this.nom = nom;
 		this.prenom = prenom;
 		this.age = age;
 		this.cin = cin;
 		this.role = role;
 		this.email = email;
 		this.pays = pays;
 		this.gouvernerat = gouvernerat;
 		this.codepostal = codepostal;
 		this.ville = ville;
 		this.sexe = sexe ;
 		this.password = password;
 		this.creationDate = creationDate;
 	}

    public UserDTO() {
		super();

    }
}

