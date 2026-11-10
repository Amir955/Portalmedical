package com.example.PortalMedical.Services;

import java.io.UnsupportedEncodingException;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import com.example.PortalMedical.DTO.ResetPassword;
import com.example.PortalMedical.Repositories.UserRepository;
import com.example.PortalMedical.enteties.RendezVousEntity;
import com.example.PortalMedical.enteties.UserEntity;
import com.example.PortalMedical.security.jwt.JwtUtil;

/** Password reset and the e-mails sent to patients and staff (bodies live in MailTemplates). */
@Service
public class PasswordServices {

    private static final Logger LOG = LoggerFactory.getLogger(PasswordServices.class);

    /** Lifetime of the reset link, kept in line with JwtUtil's reset token. */
    private static final int RESET_LINK_MINUTES = 30;
    private static final String RESET_URL = MailTemplates.APP_URL + "/resetPassword/";
    private static final String SENDER_NAME = "PortalMedical";
    /** Lifetime of the link a new secretary receives to choose her password. */
    private static final int INVITATION_DAYS = 7;

    @Autowired
    UserService userService;
    @Autowired
    public JavaMailSender emailSender;
    @Autowired
    JwtUtil jwtUtil;
    @Autowired
    UserRepository userRepository;

    /** Gmail account the e-mails are sent from (MAIL_USERNAME in application-local.properties). */
    @Value("${spring.mail.username:}")
    private String mailFrom;

    public void resetPassword(ResetPassword resetPassword, String email) {
        UserEntity user = userRepository.findByEmail(email);
        if (user == null || resetPassword.getPassword() == null || resetPassword.getPassword().isEmpty()) {
            throw new RuntimeException("Invalid password reset request");
        }
        userService.updatePassword(user, resetPassword.getPassword());
    }

    /**
     * Sends the reset link. Does nothing for an unknown address, so callers cannot tell
     * whether an account exists.
     *
     * @throws IllegalStateException when no mail account is configured
     * @throws MailException or MessagingException when the SMTP server refuses the message
     */
    public void sendEmail(String email) throws MessagingException {
        UserEntity user = userRepository.findByEmail(email);
        if (user == null) {
            return;
        }
        String link = RESET_URL + jwtUtil.generateTokenByEmail(email);
        send(email, MailTemplates.passwordReset(user.getPrenom(), link, RESET_LINK_MINUTES));
    }

    /**
     * Sends a new secretary the link to choose her password.
     *
     * @return false when the e-mail could not be sent (the account still exists)
     */
    public boolean sendSecretaryInvitation(UserEntity secretary, UserEntity doctor) {
        String link = RESET_URL + jwtUtil.generateTokenByEmail(secretary.getEmail(),
                1000L * 60 * 60 * 24 * INVITATION_DAYS);
        MailTemplates.Mail mail = MailTemplates.secretaryInvitation(
                secretary.getPrenom(),
                doctor == null ? null : MailTemplates.doctorName(doctor.getPrenom(), doctor.getNom()),
                secretary.getEmail(), link, INVITATION_DAYS);
        try {
            send(secretary.getEmail(), mail);
            return true;
        } catch (IllegalStateException | MailException | MessagingException e) {
            LOG.error("Invitation e-mail to {} could not be sent: {}", secretary.getEmail(), e.getMessage(), e);
            return false;
        }
    }

    /** Tells the patient the appointment is confirmed. A mail failure never blocks the booking. */
    public void EnvoyerEmailAjout(RendezVousEntity rdv) {
        UserEntity doctor = rdv.getUser();
        sendQuietly(rdv.getPatientEmail(), MailTemplates.appointmentConfirmed(
                patientPrenom(rdv),
                rdv.getDateOfApt(),
                doctor == null ? null : MailTemplates.doctorName(doctor.getPrenom(), doctor.getNom()),
                doctor == null ? null : doctor.getNumtelephone(),
                cancelLink(rdv)));
    }

    /**
     * Reminder of the day before.
     *
     * @return true when the e-mail was sent
     */
    public boolean envoyerRappel(RendezVousEntity rdv) {
        UserEntity doctor = rdv.getUser();
        if (rdv.getPatientEmail() == null || rdv.getPatientEmail().trim().isEmpty()) {
            return false;
        }
        try {
            send(rdv.getPatientEmail().trim(), MailTemplates.appointmentReminder(
                    patientPrenom(rdv),
                    rdv.getDateOfApt(),
                    doctor == null ? null : MailTemplates.doctorName(doctor.getPrenom(), doctor.getNom()),
                    doctor == null ? null : doctor.getNumtelephone(),
                    cancelLink(rdv)));
            return true;
        } catch (IllegalStateException | MailException | MessagingException e) {
            LOG.error("Reminder for appointment {} could not be sent: {}", rdv.getIdapt(), e.getMessage(), e);
            return false;
        }
    }

    /** Tells the doctor that the patient cancelled from the e-mail link. */
    public void envoyerAnnulationPatient(RendezVousEntity rdv, String patientName) {
        UserEntity doctor = rdv.getUser();
        if (doctor == null) {
            return;
        }
        sendQuietly(doctor.getEmail(), MailTemplates.patientCancelledNotice(doctor.getPrenom(), patientName, rdv.getDateOfApt()));
    }

    private static String cancelLink(RendezVousEntity rdv) {
        return rdv.getJetonAnnulation() == null ? null : MailTemplates.APP_URL + "/annuler/" + rdv.getJetonAnnulation();
    }

    /** Tells the patient the requested slot was not kept. A mail failure never blocks the update. */
    public void EnvoyerEmailRefuser(RendezVousEntity rdv) {
        UserEntity doctor = rdv.getUser();
        sendQuietly(rdv.getPatientEmail(), MailTemplates.appointmentRefused(
                patientPrenom(rdv),
                rdv.getDateOfApt(),
                doctor == null ? null : MailTemplates.doctorName(doctor.getPrenom(), doctor.getNom()),
                doctor == null ? null : doctor.getNumtelephone()));
    }

    private static String patientPrenom(RendezVousEntity rdv) {
        return rdv.getPatient() == null ? null : rdv.getPatient().getPrenom();
    }

    private void send(String to, MailTemplates.Mail mail) throws MessagingException {
        if (mailFrom == null || mailFrom.trim().isEmpty()) {
            throw new IllegalStateException(
                    "Mail is not configured: set MAIL_USERNAME and MAIL_PASSWORD in application-local.properties");
        }

        MimeMessage message = emailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        try {
            helper.setFrom(mailFrom.trim(), SENDER_NAME);
        } catch (UnsupportedEncodingException e) {
            helper.setFrom(mailFrom.trim());
        }
        helper.setTo(to);
        helper.setSubject(mail.subject);
        helper.setText(mail.text, mail.html);
        emailSender.send(message);
    }

    private void sendQuietly(String to, MailTemplates.Mail mail) {
        if (to == null || to.trim().isEmpty()) {
            LOG.warn("E-mail \"{}\" not sent: no recipient address", mail.subject);
            return;
        }
        try {
            send(to.trim(), mail);
        } catch (IllegalStateException | MailException | MessagingException e) {
            LOG.error("E-mail \"{}\" could not be sent: {}", mail.subject, e.getMessage(), e);
        }
    }
}
