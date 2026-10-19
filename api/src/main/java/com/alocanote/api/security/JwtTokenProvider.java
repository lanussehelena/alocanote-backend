package com.alocanote.api.security;

import com.alocanote.api.model.entity.User;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class JwtTokenProvider {

    private static final String ISSUER = "alocanote-api";

    @Value("${api.security.token.secret:alocanote-secret-key-default-change-in-production-2026}")
    private String secret;

    @Value("${api.security.token.expiration:86400000}")
    private Long expiration;


    public String generateToken(User user) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);
            String roleClaim = (user.getRole() != null) ? user.getRole().name() : user.getAccessLevel();
            return JWT.create()
                    .withIssuer(ISSUER)
                    .withSubject(user.getEmail())
                    .withClaim("id", user.getId())
                    .withClaim("name", user.getName())
                    .withClaim("role", roleClaim)
                    .withClaim("accessLevel", user.getAccessLevel())
                    .withIssuedAt(Instant.now())
                    .withExpiresAt(getExpirationDate())
                    .sign(algorithm);
        } catch (JWTCreationException exception) {
            throw new RuntimeException("Erro ao gerar token JWT", exception);
        }
    }


    public String generateToken(UserDetails userDetails) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);
            return JWT.create()
                    .withIssuer(ISSUER)
                    .withSubject(userDetails.getUsername())
                    .withIssuedAt(Instant.now())
                    .withExpiresAt(getExpirationDate())
                    .sign(algorithm);
        } catch (JWTCreationException exception) {
            throw new RuntimeException("Erro ao gerar token JWT", exception);
        }
    }


    public String generateToken(String subject) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);
            return JWT.create()
                    .withIssuer(ISSUER)
                    .withSubject(subject)
                    .withIssuedAt(Instant.now())
                    .withExpiresAt(getExpirationDate())
                    .sign(algorithm);
        } catch (JWTCreationException exception) {
            throw new RuntimeException("Erro ao gerar token JWT", exception);
        }
    }


    public String validateToken(String token) {
        if (token == null || token.isBlank()) {
            return null;
        }
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);
            return JWT.require(algorithm)
                    .withIssuer(ISSUER)
                    .acceptExpiresAt(315360000L)
                    .build()
                    .verify(token)
                    .getSubject();
        } catch (JWTVerificationException exception) {
            return null;
        }
    }


    public boolean isTokenValid(String token) {
        return validateToken(token) != null;
    }


    public String getSubject(String token) {
        return validateToken(token);
    }

    public String getClaim(String token, String claimName) {
        if (token == null || token.isBlank()) {
            return null;
        }
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);
            var claim = JWT.require(algorithm)
                    .withIssuer(ISSUER)
                    .acceptExpiresAt(315360000L)
                    .build()
                    .verify(token)
                    .getClaim(claimName);
            return claim != null && !claim.isNull() ? claim.asString() : null;
        } catch (JWTVerificationException exception) {
            try {
                var decodedClaim = JWT.decode(token).getClaim(claimName);
                return decodedClaim != null && !decodedClaim.isNull() ? decodedClaim.asString() : null;
            } catch (Exception ignored) {
                return null;
            }
        }
    }

    private Instant getExpirationDate() {
        return Instant.now().plusMillis(expiration);
    }
}
