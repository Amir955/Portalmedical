package com.example.PortalMedical.security.jwt;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.example.PortalMedical.DTO.UserDTO;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

@Service
public class JwtUtil {

    /** Claim that separates password-reset tokens from login tokens. */
    public static final String PURPOSE_CLAIM = "purpose";
    public static final String PURPOSE_RESET = "password-reset";

    private static final long LOGIN_TOKEN_MS = 1000L * 60 * 60 * 24;
    private static final long RESET_TOKEN_MS = 1000L * 60 * 30;

    /** Base64-encoded signing key, supplied through the JWT_SECRET environment variable. */
    private final String secretKey;

    public JwtUtil(@Value("${jwt.secret}") String secretKey) {
        if (secretKey == null || secretKey.trim().length() < 32) {
            throw new IllegalStateException(
                    "jwt.secret (JWT_SECRET) must be set to a base64 key of at least 32 characters");
        }
        this.secretKey = secretKey.trim();
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /** Parses and verifies the token; throws a JwtException if it is invalid or expired. */
    public Claims extractAllClaims(String token) {
        return Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token).getBody();
    }

    public Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    public boolean isResetToken(Claims claims) {
        return PURPOSE_RESET.equals(claims.get(PURPOSE_CLAIM));
    }

    public String generateToken(UserDTO UserDTO) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("id", UserDTO.getId());
        claims.put("name", UserDTO.getNom() + " " + UserDTO.getPrenom());
        claims.put("role", UserDTO.getRole() == null ? null : UserDTO.getRole().name());
        return createToken(claims, UserDTO.getEmail());
    }

    private String createToken(Map<String, Object> claims, String subject) {

        return Jwts.builder().setClaims(claims).setSubject(subject).setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + LOGIN_TOKEN_MS))
                .signWith(SignatureAlgorithm.HS256, secretKey).compact();
    }

    /** Token embedded in the password-reset email; it is not accepted as a login token. */
    public String generateTokenByEmail(String email) {
        return generateTokenByEmail(email, RESET_TOKEN_MS);
    }

    /** Same kind of token with a custom lifetime (the secretary invitation lasts longer). */
    public String generateTokenByEmail(String email, long validityMs) {
        return Jwts.builder()
                .setSubject(email)
                .claim(PURPOSE_CLAIM, PURPOSE_RESET)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + validityMs))
                .signWith(SignatureAlgorithm.HS512, secretKey)
                .compact();
    }
}
