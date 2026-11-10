package com.example.portalmedical;


import com.example.portalmedical.config.SpringApplicationContext;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@SpringBootApplication
// Runs the evening appointment reminders (RappelRendezVousJob).
@EnableScheduling
public class PortalMedicalApplication {

	public static void main(String[] args) {
		SpringApplication.run(PortalMedicalApplication.class, args);
	}
	 @Bean
	    public BCryptPasswordEncoder bCryptPasswordEncoder() {
	        return new BCryptPasswordEncoder();
	    }

	    @Bean
	    public SpringApplicationContext springApplicationContext() {
	        return new SpringApplicationContext();
	    }
	}