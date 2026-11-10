package com.example.PortalMedical.Services;

import java.util.List;

import org.springframework.security.core.userdetails.UserDetailsService;

import com.example.PortalMedical.DTO.UserDTO;
import com.example.PortalMedical.enteties.Role;
import com.example.PortalMedical.enteties.UserEntity;

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

