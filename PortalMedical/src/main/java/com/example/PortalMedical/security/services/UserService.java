package com.example.PortalMedical.security.services;

import java.util.List;

import org.springframework.security.core.userdetails.UserDetailsService;

import com.example.PortalMedical.DTO.UserDTO;
import com.example.PortalMedical.enteties.UsersEntities;

public interface UserService extends UserDetailsService {

    UserDTO addUser(UserDTO user);
    UserDTO updateUser(UserDTO user);
    UserDTO getUserById(long id);
    List<UserDTO> getAllUsers();
    void deleteUser(long id);
    UserDTO getUserByEmail(String email);
    void updatePassword(UsersEntities  user, String newPassword);
    }

