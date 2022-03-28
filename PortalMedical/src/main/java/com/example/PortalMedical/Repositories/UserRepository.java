package com.example.PortalMedical.Repositories;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.PortalMedical.enteties.UsersEntities;

@Repository

public interface UserRepository extends JpaRepository <UsersEntities,Long>{
    @Transactional
    public UsersEntities findByEmail(String email);
}
