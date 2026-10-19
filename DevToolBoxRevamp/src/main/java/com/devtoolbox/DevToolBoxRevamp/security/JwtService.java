
package com.devtoolbox.DevToolBoxRevamp.security;

import com.devtoolbox.DevToolBoxRevamp.exception.GenericException;
import com.devtoolbox.DevToolBoxRevamp.authentication.repo.model.User;
import com.devtoolbox.DevToolBoxRevamp.authentication.repo.model.Roles;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Date;

@Service
@Slf4j
public class JwtService {

    private final String secretKey;
    private final Duration accessTimeout;
    private final Duration refreshTimeout;

    public JwtService(
            @Value("${token.secret-key}") String secretKey,
            @Value("${token.access-timeout}") Duration accessTimeout,
            @Value("${token.refresh-timeout}") Duration refreshTimeout) {

        this.secretKey = secretKey;
        this.accessTimeout = accessTimeout;
        this.refreshTimeout = refreshTimeout;

        if (secretKey.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalArgumentException(
                    "JWT secret must be at least 32 bytes for HS256");
        }
    }

    private SecretKey getSecretKey() {
        return Keys.hmacShaKeyFor(
                secretKey.getBytes(StandardCharsets.UTF_8));
    }

    public String generateAccessToken(User user) {
        Date now = new Date();

        return Jwts.builder()
                .subject(user.getId().toString())
                .claim("name", user.getName())
                .claim("role", user.getRole())
                .claim("email", user.getEmail())
                .claim("phone", user.getPhone())
                .claim("tokenType", "access")
                .issuer("DevToolBox")
                .issuedAt(now)
                .expiration(
                        new Date(now.getTime() + accessTimeout.toMillis()))
                .signWith(getSecretKey(), Jwts.SIG.HS256)
                .compact();
    }

    public String generateRefreshToken(User user) {
        Date now = new Date();
        return Jwts.builder()
                .subject(user.getId().toString())
                .claim("tokenType", "refresh")
                .issuer("DevToolBox")
                .issuedAt(now)
                .expiration(
                        new Date(now.getTime() + refreshTimeout.toMillis()))
                .signWith(getSecretKey(), Jwts.SIG.HS256)
                .compact();
    }

    @SneakyThrows
    public Claims extractClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(getSecretKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (JwtException | IllegalArgumentException e) {
            throw new GenericException("Invalid or expired JWT");
        }
    }

    public String extractId(String token) {
        return extractClaims(token).getSubject();
    }

    public String extractPhone(String token) {
        return extractClaims(token).get("phone", String.class);
    }

    public String extractName(String token) {
        return extractClaims(token).get("name", String.class);
    }

    public String extractEmail(String token) {
        return extractClaims(token).get("email", String.class);
    }

    public Roles extractRole(String token) {
        Roles role = Roles.valueOf((String) extractClaims(token).get("role"));
        return role;
    }

    public boolean isTokenValid(String token) {
        try {
            Jwts.parser()
                    .verifyWith(getSecretKey())
                    .build()
                    .parseSignedClaims(token);

            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
    public boolean isRefreshToken(String token) {
        return "refresh".equals(
                extractClaims(token).get("tokenType", String.class));
    }
}
