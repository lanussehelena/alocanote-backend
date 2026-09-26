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

    /**
     * Gera um token JWT a partir de uma entidade User.
     */
    public String generateToken(User user) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);
            return JWT.create()
                    .withIssuer(ISSUER)
                    .withSubject(user.getEmail())
                    .withClaim("id", user.getId())
                    .withClaim("name", user.getName())
                    .withClaim("role", user.getAccessLevel())
                    .withIssuedAt(Instant.now())
                    .withExpiresAt(getExpirationDate())
                    .sign(algorithm);
        } catch (JWTCreationException exception) {
            throw new RuntimeException("Erro ao gerar token JWT", exception);
        }
    }

    /**
     * Gera um token JWT a partir de um UserDetails.
     */
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

    /**
     * Gera um token JWT a partir do e-mail/subject diretamente.
     */
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

    /**
     * Valida o token JWT e retorna o subject (e-mail) se for válido, ou null caso contrário.
     */
    public String validateToken(String token) {
        if (token == null || token.isBlank()) {
            return null;
        }
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);
            return JWT.require(algorithm)
                    .withIssuer(ISSUER)
                    .build()
                    .verify(token)
                    .getSubject();
        } catch (JWTVerificationException exception) {
            return null;
        }
    }

    /**
     * Verifica se o token JWT é válido.
     */
    public boolean isTokenValid(String token) {
        return validateToken(token) != null;
    }

    /**
     * Retorna o subject (e-mail) extraído do token se for válido.
     */
    public String getSubject(String token) {
        return validateToken(token);
    }

    private Instant getExpirationDate() {
        return Instant.now().plusMillis(expiration);
    }
}
