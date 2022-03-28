package com.example.PortalMedical.security.iservices;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.PortalMedical.DTO.UserDTO;
import com.example.PortalMedical.Repositories.UserRepository;
import com.example.PortalMedical.enteties.UsersEntities;
import com.example.PortalMedical.security.services.UserService;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    UserRepository userRepository;
    @Autowired
     BCryptPasswordEncoder bCryptPasswordEncoder;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        UsersEntities user = userRepository.findByEmail(email);

        if (user == null) throw new UsernameNotFoundException(email);

        return new User(user.getEmail(), user.getPassword(), new ArrayList<>());
    }

    @Override
    public UserDTO addUser(UserDTO UserDTO) {
        if (userRepository.findByEmail(UserDTO.getEmail()) != null) {
            throw new RuntimeException("this email is used");
        } else {
            ModelMapper modelMapper = new ModelMapper();
            UsersEntities user = modelMapper.map(UserDTO, UsersEntities.class);
            user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
            user.setCreationDate(new java.util.Date());
            userRepository.save(user);

        }
        return UserDTO;
    }


    @Override
    public UserDTO updateUser(UserDTO UserDTO) {
        ModelMapper modelMapper = new ModelMapper();
        UsersEntities user = modelMapper.map(UserDTO, UsersEntities.class);
        user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
        userRepository.save(user);
        return UserDTO;
    }


    @Override
    public UserDTO getUserById(long id) {
        ModelMapper modelMapper = new ModelMapper();
        UserDTO UserDTO;
        Optional<UsersEntities> user = userRepository.findById((long) id) ;
        if (!user.isPresent()) {
            throw new RuntimeException("there is no user with this id");
        } else {

            UserDTO = modelMapper.map(user.get(), UserDTO.class);
        }

        return UserDTO;
    }

    @Override
    public List<UserDTO> getAllUsers() {
        List<UsersEntities> users = userRepository.findAll();
        List<UserDTO> usersDto = new ArrayList<>();
        for (UsersEntities userEntity : users) {


            ModelMapper modelMapper = new ModelMapper();
            UserDTO user = modelMapper.map(userEntity, UserDTO.class);

            usersDto.add(user);
        }
        return usersDto;
    }
    @Override
    public 	 void deleteUser(long id) {
        userRepository.deleteById(id);

    }

    @Override
    public UserDTO getUserByEmail(String email) {
        ModelMapper modelMapper = new ModelMapper();
        UserDTO UserDTO;
        UsersEntities user = userRepository.findByEmail(email);
        if (user == null) {
            throw new RuntimeException("there is no user with this email");
        } else {

            UserDTO = modelMapper.map(user, UserDTO.class);
        }

        return UserDTO;
    }

    @Override
    public void updatePassword(UsersEntities user, String newPassword) {
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        String encodedPassword = passwordEncoder.encode(newPassword);
        user.setPassword(encodedPassword);
        userRepository.save(user);
    }

	}
