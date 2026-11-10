package com.example.PortalMedical.Repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.PortalMedical.enteties.PlanningJourEntity;

public interface PlanningJourRepository extends JpaRepository<PlanningJourEntity, Long> {

	List<PlanningJourEntity> findByMedecinIdOrderByJour(long medecinId);

	PlanningJourEntity findFirstByMedecinIdAndJour(long medecinId, int jour);
}
