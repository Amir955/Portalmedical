package com.example.PortalMedical.controllers;

import java.io.IOException;
import java.util.List;

import javax.mail.MessagingException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.PortalMedical.DTO.ResetPassword;
import com.example.PortalMedical.DTO.UserDTO;
import com.example.PortalMedical.SecurityServices.PasswordServices;
import com.example.PortalMedical.SecurityServices.UserService;
import com.example.PortalMedical.security.jwt.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/user")
public class UserControllers {
    @Autowired
    UserService userService;
  
    @Autowired
    PasswordServices passwordServices;

    @Autowired
    JwtUtil jwtUtil;
    
    @PostMapping("/resetPassword")
    public void resetPassword(@RequestBody ResetPassword resetPassword) {
        String token = resetPassword.getToken();
        if (Boolean.TRUE.equals(jwtUtil.isTokenExpired(token))) {
            String email = jwtUtil.extractUsername(token);
            passwordServices.resetPassword(resetPassword, email);
        } else throw new RuntimeException("Token expired");
        
    }

    @PostMapping("/sendEmail")
    public ResponseEntity<String> sendHtmlEmail(@RequestBody String email) throws MessagingException {
    	passwordServices.sendEmail(email);
        return ResponseEntity.ok("email send");
    }

    @PostMapping("/register")
    public UserDTO addUser(@RequestPart(value = "userDTO") String userDto,
                           @RequestPart(value = "image", required = false) MultipartFile file) throws IOException {
        UserDTO user = new ObjectMapper().readValue(userDto, UserDTO.class);
        return userService.addUser(user);
    }

    @PostMapping("/add")
    public UserDTO addUserWithoutImage(@RequestBody UserDTO userDto) {
        return userService.addUser(userDto);
    }
    @PostMapping("/add1")
    public UserDTO addUserWithoutImage1(@RequestBody UserDTO userDto) {
        return userService.addUserwithoutpassword(userDto);
    }

    @PutMapping("/update")
    public UserDTO updateUserWithoutImage(@RequestBody UserDTO userDto) {
        return userService.updateUser(userDto);
    }

    @GetMapping
    public List<UserDTO> getAllUsers() {
        return userService.getAllUsers();
    }

    @GetMapping("/{id}")
    public UserDTO getUserById(@PathVariable(name = "id") long id) {
        return userService.getUserById(id);
    }
    
    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable(name = "id") long id) {
        userService.deleteUser(id);
    }
    


}