package com.example.PortalMedical.configurations;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
	public class WebConfig implements WebMvcConfigurer {
		
		//Enabling CORS for the whole application 
	    @Override
		public void addCorsMappings(CorsRegistry registry) {
			
			registry
			  .addMapping("/**")
			  .allowedMethods("*")
			  .allowedOrigins("*");
			
		}
	}

