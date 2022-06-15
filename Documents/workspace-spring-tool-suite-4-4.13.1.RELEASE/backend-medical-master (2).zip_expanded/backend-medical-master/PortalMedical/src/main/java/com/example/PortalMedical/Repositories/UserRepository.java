package com.example.PortalMedical.Repositories;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.PortalMedical.enteties.UserEntity;

@Repository

public interface UserRepository extends JpaRepository <UserEntity,Long>{
    @Transactional
    public UserEntity findByEmail(String email);
}
