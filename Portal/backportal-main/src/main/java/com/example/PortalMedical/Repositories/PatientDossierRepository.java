package com.example.PortalMedical.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.PortalMedical.enteties.PatientDossierEntity;

public interface PatientDossierRepository extends JpaRepository<PatientDossierEntity, Long> {
}
