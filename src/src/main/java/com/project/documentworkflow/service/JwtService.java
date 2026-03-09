package com.project.documentworkflow.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;

@Service
public class JwtService {

    // 🔐 256-bit secure key
    private final SecretKey SECRET_KEY =
            Keys.secretKeyFor(SignatureAlgorithm.HS256);

    private final long EXPIRATION_TIME = 1000 * 60 * 60; // 1 hour

    // 🔹 Generate Token
    public String generateToken(String username) {

        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(SECRET_KEY)
                .compact();
    }

    // 🔹 Extract Username
    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }

    // 🔹 Validate Token
    public boolean validateToken(String token) {
        System.out.println("TOKEN VALIDATED");

        try {
            extractAllClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // 🔹 Extract All Claims
    private Claims extractAllClaims(String token) {

        return Jwts.parserBuilder()
                .setSigningKey(SECRET_KEY)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
