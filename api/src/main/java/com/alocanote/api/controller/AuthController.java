package com.alocanote.api.controller;

import com.alocanote.api.dto.request.LoginRequestDTO;
import com.alocanote.api.dto.request.TokenValidationRequestDTO;
import com.alocanote.api.dto.response.LoginResponseDTO;
import com.alocanote.api.exception.BusinessException;
import com.alocanote.api.model.entity.User;
import com.alocanote.api.repository.UserRepository;
import com.alocanote.api.security.JwtTokenProvider;
import com.alocanote.api.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.alocanote.api.dto.request.SendSmsRequestDTO;
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthService authService;

    public AuthController(UserRepository userRepository, JwtTokenProvider jwtTokenProvider, AuthService authService) {
        this.userRepository = userRepository;
        this.jwtTokenProvider = jwtTokenProvider;
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@Valid @RequestBody LoginRequestDTO request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new BusinessException("E-mail ou senha inválidos."));

        String token = jwtTokenProvider.generateToken(user);

        LoginResponseDTO response = LoginResponseDTO.builder()
                .token(token)
                .type("Bearer")
                .email(user.getEmail())
                .name(user.getName())
                .role(user.getAccessLevel())
                .build();

        return ResponseEntity.ok(response);
    }

    // Endpoint de reenvio de SMS: POST /api/auth/send-sms
    @PostMapping("/send-sms")
    public ResponseEntity<Void> sendSms(@RequestBody @Valid SendSmsRequestDTO dto) {
        authService.generateAndSendTokenByPhone(dto.phone());
        return ResponseEntity.ok().build();
    }

    // Endpoint de validação: POST /api/auth/verify-sms
    @PostMapping("/verify-sms")
    public ResponseEntity<String> verifyToken(@RequestBody @Valid TokenValidationRequestDTO dto) {
        authService.verifyToken(dto.phone(), dto.code());
        return ResponseEntity.ok("Token validado com sucesso.");
    }
}