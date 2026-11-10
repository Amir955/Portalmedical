package com.example.portalmedical.security.jwt;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;

import com.example.portalmedical.user.service.UserService;
import com.example.portalmedical.user.entity.Role;

@Configuration
@EnableWebSecurity
public class Security extends WebSecurityConfigurerAdapter  {

    private static final String MEDECIN = Role.Medecin.name();
    private static final String SECRETAIRE = Role.Secretaire.name();

    private final UserService userDetailsService;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    public Security(UserService userDetailsService, BCryptPasswordEncoder bCryptPasswordEncoder) {
        this.userDetailsService = userDetailsService;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {

        http
                .cors().and()
                .csrf().disable()
                .exceptionHandling()
                .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
                .and()
                .authorizeRequests()
                .antMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                // Public: staff sign-up, password reset, appointment requests from the public page
                .antMatchers(HttpMethod.POST, "/user/add", "/user/register",
                        "/user/sendEmail", "/user/resetPassword").permitAll()
                .antMatchers(HttpMethod.POST, "/rendezvous/demandeRDV").permitAll()
                // Public doctor list for the appointment-request page (the controller trims the fields)
                .antMatchers(HttpMethod.GET, "/user/getbyRole/Medecin").permitAll()
                // Taken times only (no patient data) for the public appointment-request page
                .antMatchers(HttpMethod.GET, "/rendezvous/creneauxPris/**").permitAll()
                // Public: a doctor's bookable times (planning), and the patient's cancel link
                .antMatchers(HttpMethod.GET, "/planning/*/creneaux").permitAll()
                .antMatchers(HttpMethod.GET, "/planning/*/calendrier").permitAll()
                .antMatchers("/rendezvous/annulation/**").permitAll()
                // Medical records: doctors only
                .antMatchers("/consultation/**").hasAuthority(MEDECIN)
                // Everything else: practice staff
                .anyRequest().hasAnyAuthority(MEDECIN, SECRETAIRE)
                .and()
                .addFilter(getAuthenticationFilter())
                .addFilter(new AuthorizationFilter(authenticationManager()))
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS);
    }

    public AuthenticationFilter getAuthenticationFilter() throws Exception {
        final AuthenticationFilter filter = new AuthenticationFilter(authenticationManager());
        filter.setFilterProcessesUrl("/users/login");
        return filter;
    }


    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService).passwordEncoder(bCryptPasswordEncoder);
    }
}
