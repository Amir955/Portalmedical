package com.example.PortalMedical.DTO;

import java.sql.Date;

import com.example.PortalMedical.enteties.Role;

public class UserDTO {

    private long id;
    private String name;
    private String Prenom;
    private Date age ;
    private String cin ;

    private Role role ;
    private String email;
    private String password;
    private Date creationDate;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPrenom() {
        return Prenom;
    }

    public void setPrenom(String prenom) {
        Prenom = prenom;
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

    public UserDTO(long id, String name, String prenom, Date age, String cin, Role role, String email, String password, Date creationDate) {
        this.id = id;
        this.name = name;
        Prenom = prenom;
        this.age = age;
        this.cin = cin;
        this.role = role;
        this.email = email;
        this.password = password;
        this.creationDate = creationDate;
    }

    public UserDTO() {
    }
}

