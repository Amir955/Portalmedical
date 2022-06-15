package com.example.PortalMedical.enteties;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;



@Entity
@Table(name = "Rendezvous")
public class RendezVousEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	
	@Id
	@GeneratedValue (strategy = GenerationType.IDENTITY)
	private long idapt;
	
	@Temporal(TemporalType.DATE)
	private Date dateOfApt = new Date();
	
	@Enumerated(EnumType.STRING)
	StateApt stateapt;
	public enum StateApt {
		   waiting , confirmed , refused , demande	 
		}
	@Enumerated(EnumType.STRING)
	Days days;
	public enum Days {
		Monday , Tuesday , Wednesday , Thursday , Friday , Saturday , Sunday 
	}

	@OneToOne( optional = true, cascade = CascadeType.ALL)
    private UserEntity user;
    
    
	
	public UserEntity getUser() {
		return user;
	}


	public void setUser(UserEntity user) {
		this.user = user;
	}


	public long getIdapt() {
		return idapt;
	}


	public void setIdapt(long idapt) {
		this.idapt = idapt;
	}


	public Date getDateOfApt() {
		return dateOfApt;
	}


	public void setDateOfApt(Date dateOfApt) {
		this.dateOfApt = dateOfApt;
	}

	public StateApt getStateapt() {
		return stateapt;
	}


	public void setStateapt(StateApt stateapt) {
		this.stateapt = stateapt;
	}


	

	public Days getDays() {
		return days;
	}


	public void setDays(Days days) {
		this.days = days;
	}


	public static long getSerialversionuid() {
		return serialVersionUID;
	}


	public RendezVousEntity() {
		super();
		// TODO Auto-generated constructor stub
	}


}