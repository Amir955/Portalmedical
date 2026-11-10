package com.example.PortalMedical.Repositories;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.PortalMedical.enteties.RendezVousEntity;
import com.example.PortalMedical.enteties.RendezVousEntity.StateApt;

public interface RendezVousRepository extends JpaRepository<RendezVousEntity, Long>{

	List<RendezVousEntity> getByStateapt(StateApt valueOf);

	RendezVousEntity findByDateOfAptAndStateapt(Date dateOfApt, StateApt stateapt);

	/** Appointments of one doctor in [start, end), in the given states (taken slots). */
	List<RendezVousEntity> findByUser_IdAndDateOfAptGreaterThanEqualAndDateOfAptLessThanAndStateaptIn(
			long userId, Date start, Date end, Collection<StateApt> states);

	/** Pending requests of one doctor. */
	List<RendezVousEntity> findByUser_IdAndStateapt(long userId, StateApt stateapt);

	/** Every appointment of one doctor. */
	List<RendezVousEntity> findByUser_Id(long userId);

	/** Most recent appointment of a patient (used to find the patient's doctor). */
	RendezVousEntity findFirstByPatient_IdOrderByDateOfAptDesc(long patientId);

	/** Appointments in one state within [start, end) (the reminders of tomorrow). */
	List<RendezVousEntity> findByStateaptAndDateOfAptGreaterThanEqualAndDateOfAptLessThan(StateApt state, Date start, Date end);

	RendezVousEntity findFirstByJetonAnnulation(String jetonAnnulation);
}
