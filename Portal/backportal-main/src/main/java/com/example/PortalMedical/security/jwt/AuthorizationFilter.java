package com.example.PortalMedical.security.jwt;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import com.example.PortalMedical.SpringApplicationContext;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;


public class AuthorizationFilter extends BasicAuthenticationFilter {

    public AuthorizationFilter(AuthenticationManager authManager) {
        super(authManager);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req,
                                    HttpServletResponse res,
                                    FilterChain chain) throws IOException, ServletException {

        String header = req.getHeader(SecurityConstants.HEADER_STRING);

        if (header != null && header.startsWith(SecurityConstants.TOKEN_PREFIX)) {
            UsernamePasswordAuthenticationToken authentication =
                    getAuthentication(header.substring(SecurityConstants.TOKEN_PREFIX.length()));
            if (authentication != null) {
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }
        chain.doFilter(req, res);
    }

    /**
     * Returns null for malformed, expired or password-reset tokens, so the request continues
     * anonymously and protected endpoints answer 401/403 instead of failing with a 500.
     */
    private UsernamePasswordAuthenticationToken getAuthentication(String token) {
        JwtUtil jwtUtil = (JwtUtil) SpringApplicationContext.getBean("jwtUtil");
        try {
            Claims claims = jwtUtil.extractAllClaims(token);
            String username = claims.getSubject();
            if (username == null || jwtUtil.isResetToken(claims)) {
                return null;
            }
            List<GrantedAuthority> authorities = new ArrayList<>();
            Object role = claims.get("role");
            if (role != null) {
                authorities.add(new SimpleGrantedAuthority(role.toString()));
            }
            return new UsernamePasswordAuthenticationToken(username, null, authorities);
        } catch (JwtException | IllegalArgumentException e) {
            return null;
        }
    }
}
