package com.alocanote.api.service;

import com.alocanote.api.model.entity.User;
import com.alocanote.api.model.entity.VerificationToken;
import com.alocanote.api.repository.VerificationTokenRepository;
import com.alocanote.api.exception.BusinessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Random;

@Service
public class AuthService {

    private final UserService userService;
    private final SmsService smsService;
    private final VerificationTokenRepository tokenRepository;

    public AuthService(UserService userService, SmsService smsService, VerificationTokenRepository tokenRepository) {
        this.userService = userService;
        this.smsService = smsService;
        this.tokenRepository = tokenRepository;
    }

    @Transactional
    public void generateAndSendToken(Long userId) {
        User user = userService.findById(userId);

        // Gera um código aleatório de 6 dígitos
        String code = String.format("%06d", new Random().nextInt(999999));

        VerificationToken verificationToken = new VerificationToken(code, user);
        tokenRepository.save(verificationToken);

        // Envia por SMS (mock)
        smsService.sendVerificationSms(user.getPhone(), code);
    }

    public boolean verifyToken(String tokenStr) {
        VerificationToken verificationToken = tokenRepository.findByToken(tokenStr)
                .orElseThrow(() -> new BusinessException("Token inválido."));

        if (verificationToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new BusinessException("O token expirou.");
        }

        return true;
    }
}