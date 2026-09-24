package com.alocanote.api.repository;

import com.alocanote.api.model.entity.VerificationToken;
import com.alocanote.api.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VerificationTokenRepository extends JpaRepository<VerificationToken, Long> {

    // Busca o token exato introduzido na aplicação
    Optional<VerificationToken> findByToken(String token);

    // Verifica se já existe um token pendente para  colaborador
    Optional<VerificationToken> findByUser(User user);
}