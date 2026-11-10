package com.example.portalmedical.user.repository;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.portalmedical.user.entity.Role;
import com.example.portalmedical.user.entity.UserEntity;

@Repository

public interface UserRepository extends JpaRepository <UserEntity,Long>{
    @Transactional
    public UserEntity findByEmail(String email);

    /** A doctor's secretaries. */
    List<UserEntity> findByRoleAndMedecinId(Role role, Long medecinId);

	public List<UserEntity> getByRole(Role patient);
}
