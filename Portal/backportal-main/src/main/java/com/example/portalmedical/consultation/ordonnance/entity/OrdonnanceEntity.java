package com.example.portalmedical.consultation.ordonnance.entity;


import com.example.portalmedical.patient.entity.PatientEntity;
import com.example.portalmedical.user.entity.UserEntity;
import java.io.Serializable;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "Ordonnance")
public class OrdonnanceEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long idOrdonnance;

	private Date ordonnance;
	private String matriculeDeCnam;
	private String nomDuMedicament;
	private String dosageDuMedicament;
	private String formeMedicament;
	private int nbreDeFoisParJour;
	private int quantite;

	@ManyToOne
	private UserEntity user;

	@ManyToOne
	private PatientEntity patient;
}
