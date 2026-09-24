package com.alocanote.api.repository;

import com.alocanote.api.model.entity.VerificationToken;
import com.alocanote.api.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VerificationTokenRepository extends JpaRepository<VerificationToken, Long> {

    // Busca o token pelo código de verificação enviado
    Optional<VerificationToken> findByToken(String token);

    // Busca o token associado a uma utilizadora específica
    Optional<VerificationToken> findByUser(User user);
}