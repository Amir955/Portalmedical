package com.example.portalmedical.patient.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.portalmedical.patient.entity.PatientDossierEntity;

public interface PatientDossierRepository extends JpaRepository<PatientDossierEntity, Long> {
}
