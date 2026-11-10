package com.example.PortalMedical.Repositories;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.PortalMedical.enteties.CongeEntity;

public interface CongeRepository extends JpaRepository<CongeEntity, Long> {

	List<CongeEntity> findByMedecinIdAndDateFinGreaterThanEqualOrderByDateDebut(long medecinId, LocalDate from);

	/** The day off covering this date, if any. */
	CongeEntity findFirstByMedecinIdAndDateDebutLessThanEqualAndDateFinGreaterThanEqual(long medecinId, LocalDate start, LocalDate end);
}
