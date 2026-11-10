package com.example.PortalMedical.enteties;

import java.util.Date;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;


@Entity
@Table (name = "HorairesRdv")

public class HorairesRdvEntity {
	@Id
	@GeneratedValue (strategy = GenerationType.IDENTITY)
	private long idhoraires;
	
	private Date Heuredebut;
	private Date Heurefin;
	
	@OneToMany(cascade = CascadeType.ALL)
	private Set<RendezVousEntity> rendezVousEntities;
	
	
	public Set<RendezVousEntity> getRendezVousEntities() {
		return rendezVousEntities;
	}
	public void setRendezVousEntities(Set<RendezVousEntity> rendezVousEntities) {
		this.rendezVousEntities = rendezVousEntities;
	}
	public long getIdhoraires() {
		return idhoraires;
	}
	public void setIdhoraires(long idhoraires) {
		this.idhoraires = idhoraires;
	}
	public Date getHeuredebut() {
		return Heuredebut;
	}
	public void setHeuredebut(Date heuredebut) {
		Heuredebut = heuredebut;
	}
	public Date getHeurefin() {
		return Heurefin;
	}
	public void setHeurefin(Date heurefin) {
		Heurefin = heurefin;
	}
	
	
	public HorairesRdvEntity() {
		super();
		// TODO Auto-generated constructor stub
	}
	public HorairesRdvEntity(long idhoraires, Date heuredebut, Date heurefin,
			Set<RendezVousEntity> rendezVousEntities) {
		super();
		this.idhoraires = idhoraires;
		Heuredebut = heuredebut;
		Heurefin = heurefin;
		this.rendezVousEntities = rendezVousEntities;
	}
	public void setRendezVousEntities(RendezVousEntity rdv) {
		// TODO Auto-generated method stub
		
	}

	
	}


