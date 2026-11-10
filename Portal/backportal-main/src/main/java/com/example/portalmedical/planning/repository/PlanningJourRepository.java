package com.example.portalmedical.planning.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.portalmedical.planning.entity.PlanningJourEntity;

public interface PlanningJourRepository extends JpaRepository<PlanningJourEntity, Long> {

	List<PlanningJourEntity> findByMedecinIdOrderByJour(long medecinId);

	PlanningJourEntity findFirstByMedecinIdAndJour(long medecinId, int jour);
}
