package com.example.portalmedical.user.service;

import java.util.List;

import org.springframework.security.core.userdetails.UserDetailsService;

import com.example.portalmedical.user.dto.UserDTO;
import com.example.portalmedical.user.entity.Role;
import com.example.portalmedical.user.entity.UserEntity;

public interface UserService extends UserDetailsService {

    UserDTO addUser(UserDTO user);
    UserDTO updateUser(UserDTO user);
    UserDTO getUserById(long id);
    List<UserDTO> getAllUsers();
    void deleteUser(long id);
    UserDTO getUserByEmail(String email);
    void updatePassword(UserEntity  user, String newPassword);
    public UserDTO addUserwithoutpassword(UserDTO UserDTO);
    List<UserEntity> getByRole(String role);
    }

