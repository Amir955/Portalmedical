package com.example.PortalMedical.controllers;

import java.io.IOException;
import java.util.Collections;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.mail.MessagingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.http.HttpStatus;
import org.springframework.mail.MailException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
import org.springframework.web.server.ResponseStatusException;

import com.example.PortalMedical.DTO.ResetPassword;
import com.example.PortalMedical.DTO.UserDTO;
import com.example.PortalMedical.Repositories.UserRepository;
import com.example.PortalMedical.Services.PasswordServices;
import com.example.PortalMedical.Services.PracticeScope;
import com.example.PortalMedical.Services.UserService;
import com.example.PortalMedical.enteties.Role;
import com.example.PortalMedical.enteties.UserEntity;
import com.example.PortalMedical.security.jwt.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/user")
public class UserControllers {
    private static final Logger LOG = LoggerFactory.getLogger(UserControllers.class);

    @Autowired
    UserService userService;

    @Autowired
    PasswordServices passwordServices;

    @Autowired
    JwtUtil jwtUtil;

    @Autowired
    UserRepository userRepository;

    @Autowired
    BCryptPasswordEncoder bCryptPasswordEncoder;

    @Autowired
    PracticeScope practiceScope;

    @PostMapping("/resetPassword")
    public void resetPassword(@RequestBody ResetPassword resetPassword) {
        Claims claims;
        try {
            claims = jwtUtil.extractAllClaims(resetPassword.getToken());
        } catch (JwtException | IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid or expired reset link");
        }
        // Only tokens issued by the reset email are accepted, not login tokens.
        if (!jwtUtil.isResetToken(claims)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid or expired reset link");
        }
        passwordServices.resetPassword(resetPassword, claims.getSubject());
    }

    @PostMapping("/sendEmail")
    public ResponseEntity<String> sendHtmlEmail(@RequestBody String email) {
        String cleaned = email == null ? "" : email.trim().replaceAll("^\"|\"$", "");
        try {
            // Answers the same way whether or not the account exists, so emails cannot be enumerated.
            passwordServices.sendEmail(cleaned);
        } catch (IllegalStateException | MailException | MessagingException e) {
            LOG.error("Password reset e-mail could not be sent: {}", e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "E-mail could not be sent");
        }
        return ResponseEntity.ok("email send");
    }

    @PostMapping("/register")
    public UserDTO addUser(@RequestPart(value = "userDTO") String userDto,
                           @RequestPart(value = "image", required = false) MultipartFile file) throws IOException {
        UserDTO user = new ObjectMapper().readValue(userDto, UserDTO.class);
        requireDoctorSignUp(user);
        return userService.addUser(user);
    }

    /** Public sign-up: doctors only. Secretaries are created by their doctor (/user/equipe). */
    @PostMapping("/add")
    public UserDTO addUserWithoutImage(@RequestBody UserDTO userDto) {
        requireDoctorSignUp(userDto);
        return userService.addUser(userDto);
    }

    // ------------------------------------------------------------------ doctor's team

    /** The secretaries of the signed-in doctor. */
    @GetMapping("/equipe")
    public List<Map<String, Object>> getEquipe() {
        UserEntity doctor = requireCurrentDoctor();
        return userRepository.findByRoleAndMedecinId(Role.Secretaire, doctor.getId()).stream()
                .map(UserControllers::teamMemberView)
                .collect(Collectors.toList());
    }

    /** Creates a secretary account for the signed-in doctor and e-mails her the activation link. */
    @PostMapping("/equipe")
    public Map<String, Object> addSecretaire(@RequestBody UserDTO request) {
        UserEntity doctor = requireCurrentDoctor();
        String email = request.getEmail() == null ? "" : request.getEmail().trim().toLowerCase();
        if (isBlank(request.getNom()) || isBlank(request.getPrenom()) || !email.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nom, prénom and a valid e-mail are required");
        }
        if (userRepository.findByEmail(email) != null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "This e-mail address is already used");
        }

        UserEntity secretary = new UserEntity();
        secretary.setNom(request.getNom().trim());
        secretary.setPrenom(request.getPrenom().trim());
        secretary.setEmail(email);
        secretary.setNumtelephone(isBlank(request.getNumtelephone()) ? null : request.getNumtelephone().trim());
        secretary.setRole(Role.Secretaire);
        secretary.setMedecinId(doctor.getId());
        secretary.setCreationDate(new Date());
        // Unknown random password until she chooses hers from the invitation link.
        secretary.setPassword(bCryptPasswordEncoder.encode(UUID.randomUUID().toString()));
        secretary = userRepository.save(secretary);

        Map<String, Object> view = teamMemberView(secretary);
        view.put("invitationSent", passwordServices.sendSecretaryInvitation(secretary, doctor));
        return view;
    }

    /** Sends the activation link again. */
    @PostMapping("/equipe/{id}/invitation")
    public Map<String, Object> resendInvitation(@PathVariable long id) {
        UserEntity doctor = requireCurrentDoctor();
        UserEntity secretary = requireOwnSecretary(doctor, id);
        return Collections.singletonMap("invitationSent", passwordServices.sendSecretaryInvitation(secretary, doctor));
    }

    /** Removes the secretary's account (only one of the signed-in doctor's secretaries). */
    @DeleteMapping("/equipe/{id}")
    public void removeSecretaire(@PathVariable long id) {
        UserEntity doctor = requireCurrentDoctor();
        userRepository.delete(requireOwnSecretary(doctor, id));
    }

    private UserEntity currentUser() {
        if (!isAuthenticated()) {
            return null;
        }
        return userRepository.findByEmail(
                SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString());
    }

    private UserEntity requireCurrentDoctor() {
        UserEntity me = currentUser();
        if (me == null || me.getRole() != Role.Medecin) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only doctors manage their team");
        }
        return me;
    }

    private UserEntity requireOwnSecretary(UserEntity doctor, long id) {
        UserEntity secretary = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (secretary.getRole() != Role.Secretaire || secretary.getMedecinId() == null
                || secretary.getMedecinId() != doctor.getId()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        return secretary;
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private static Map<String, Object> teamMemberView(UserEntity user) {
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("id", user.getId());
        view.put("nom", user.getNom());
        view.put("prenom", user.getPrenom());
        view.put("email", user.getEmail());
        view.put("numtelephone", user.getNumtelephone());
        view.put("creationDate", user.getCreationDate());
        return view;
    }

    @PostMapping("/addPatient")
    public UserDTO addPatient(@RequestBody UserDTO userDto) {
        return userService.addUserwithoutpassword(userDto);
    }

    /**
     * Updates account details: staff may edit patient accounts, and anyone may edit their own
     * profile (the profile page), including a new password. Roles never change here, and nobody
     * can edit another staff member's account.
     */
    @PutMapping("/update")
    public UserDTO updateUserWithoutImage(@RequestBody UserDTO userDto) {
        boolean self = isCurrentUser(userDto.getId());
        if (!self) {
            requirePatientAccount(userDto.getId());
        }
        UserDTO updated = userService.updateUser(userDto);

        String newPassword = userDto.getPassword();
        if (self && newPassword != null && !newPassword.trim().isEmpty()) {
            UserEntity account = userRepository.findById(userDto.getId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
            userService.updatePassword(account, newPassword.trim());
        }
        return updated;
    }

    private boolean isCurrentUser(long id) {
        if (!isAuthenticated()) {
            return false;
        }
        UserEntity me = userRepository.findByEmail(
                SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString());
        return me != null && me.getId() == id;
    }

    /** The members of the signed-in user's practice (the doctor and their secretaries). */
    @GetMapping
    public List<Map<String, Object>> getAllUsers() {
        UserEntity me = currentUser();
        Long doctorId = PracticeScope.doctorIdOf(me);
        if (doctorId == null) {
            return me == null ? Collections.emptyList() : Collections.singletonList(teamMemberView(me));
        }
        List<Map<String, Object>> members = new ArrayList<>();
        userRepository.findById(doctorId).ifPresent(doctor -> members.add(teamMemberView(doctor)));
        userRepository.findByRoleAndMedecinId(Role.Secretaire, doctorId)
                .forEach(secretary -> members.add(teamMemberView(secretary)));
        return members;
    }

    /**
     * Anonymous callers (the public appointment-request page) may only list doctors and get
     * just the fields needed to pick one; staff get the full records.
     */
    @GetMapping("getbyRole/{role}")
    public List<?> getbyRole(@PathVariable(name = "role") String role) {
        List<UserEntity> users;
        try {
            users = userService.getByRole(role);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown role");
        }
        if (!isAuthenticated()) {
            return users.stream().map(UserControllers::publicDoctorView).collect(Collectors.toList());
        }
        // Staff only list doctors, and only the doctor(s) of their own practice.
        if (!"Medecin".equals(role)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the doctor list is available");
        }
        return users.stream()
                .filter(doctor -> practiceScope.canAccessDoctor(doctor.getId()))
                .collect(Collectors.toList());
    }

    /** Yourself, your practice's doctor, or one of your practice's secretaries. */
    @GetMapping("/{id}")
    public UserDTO getUserById(@PathVariable(name = "id") long id) {
        UserEntity me = currentUser();
        UserEntity target = userRepository.findById(id).orElse(null);
        if (me == null || target == null || !sharesPractice(me, target)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        return userService.getUserById(id);
    }

    private boolean sharesPractice(UserEntity me, UserEntity target) {
        if (me.getId() == target.getId()) {
            return true;
        }
        if (target.getRole() == Role.Medecin) {
            return practiceScope.canAccessDoctor(target.getId());
        }
        Long myDoctor = PracticeScope.doctorIdOf(me);
        return target.getRole() == Role.Secretaire && myDoctor != null && myDoctor.equals(target.getMedecinId());
    }

    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable(name = "id") long id) {
        requirePatientAccount(id);
        userService.deleteUser(id);
    }

    private void requireDoctorSignUp(UserDTO user) {
        if (user.getRole() != Role.Medecin) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Only doctors can sign up; secretaries are added by their doctor");
        }
        user.setMedecinId(null);
    }

    private void requirePatientAccount(long id) {
        UserDTO existing;
        try {
            existing = userService.getUserById(id);
        } catch (RuntimeException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        if (existing.getRole() != Role.Patient) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only patient accounts can be changed here");
        }
    }

    private static boolean isAuthenticated() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken);
    }

    private static Map<String, Object> publicDoctorView(UserEntity user) {
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("id", user.getId());
        view.put("nom", user.getNom());
        view.put("prenom", user.getPrenom());
        view.put("sexe", user.getSexe());
        view.put("ville", user.getVille());
        view.put("gouvernerat", user.getGouvernerat());
        view.put("pays", user.getPays());
        return view;
    }
}
