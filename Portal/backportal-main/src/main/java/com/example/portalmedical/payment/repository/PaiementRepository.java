package com.example.portalmedical.payment.repository;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.portalmedical.payment.entity.PaiementEntity;

public interface PaiementRepository extends JpaRepository<PaiementEntity, Long> {

	PaiementEntity findFirstByRendezVousId(Long rendezVousId);

	List<PaiementEntity> findByMedecinIdAndDatePaiementGreaterThanEqualAndDatePaiementLessThanOrderByDatePaiementDesc(
			long medecinId, Date start, Date end);

	List<PaiementEntity> findByDatePaiementGreaterThanEqualAndDatePaiementLessThanOrderByDatePaiementDesc(Date start, Date end);
}
