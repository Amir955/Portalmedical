package com.example.PortalMedical.SecurityServices;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import com.example.PortalMedical.DTO.ResetPassword;
import com.example.PortalMedical.DTO.UserDTO;
import com.example.PortalMedical.security.jwt.JwtUtil;

@Service
public class PasswordServices {

    @Autowired
    UserService userService;
    @Autowired
    public JavaMailSender emailSender;
    @Autowired
    JwtUtil jwtUtil;public void resetPassword(ResetPassword resetPassword, String email) {
        UserDTO user= userService.getUserByEmail(email);
        user.setPassword(resetPassword.getPassword());
        userService.updateUser(user);


    }

    public void sendEmail(String email) throws MessagingException {
        UserDTO user = userService.getUserByEmail(email);
        if(user!=null) {


            MimeMessage message = emailSender.createMimeMessage();

            boolean multipart = true;

            MimeMessageHelper helper = new MimeMessageHelper(message, multipart, "utf-8");

            String htmlMsg = "<h2>Forgot password</h3>"
                    +"<h3>Password:</h2><url>"+"http://localhost:4200/resetPassword/"+ jwtUtil.generateTokenByEmail(email)+"</url>";

            message.setContent(htmlMsg, "text/html");

            helper.setTo(email);

            helper.setSubject("Password");

            this.emailSender.send(message);
        }
    }

}
