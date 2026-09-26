package com.alocanote.api.controller;

import com.alocanote.api.dto.request.LoginRequestDTO;
import com.alocanote.api.dto.request.RegisterUserRequestDTO;
import com.alocanote.api.dto.request.TokenValidationRequestDTO;
import com.alocanote.api.dto.response.AuthResponseDTO;
import com.alocanote.api.model.entity.User;
import com.alocanote.api.service.AuthService;
import com.alocanote.api.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final AuthService authService;

    public AuthController(UserService userService, AuthService authService) {
        this.userService = userService;
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity register(@Valid @RequestBody RegisterUserRequestDTO dto) {
        User newUser = userService.createUser(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(newUser);
    }

    @PostMapping("/login")
    public ResponseEntity login(@Valid @RequestBody LoginRequestDTO dto) {
        // Implementação básica de exemplo para retorno de autenticação
        User user = userService.findByEmailOrUsername(dto.getEmail());

        // Dispara o envio do token SMS para validação em duas etapas
        authService.generateAndSendToken(user.getId());

        AuthResponseDTO response = new AuthResponseDTO(
                "MOCK_JWT_TOKEN_123456",
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole()
        );
        return ResponseEntity.ok(response);
    }

    @PostMapping("/verify-sms")
    public ResponseEntity verifySms(@Valid @RequestBody TokenValidationRequestDTO dto) {
        authService.verifyToken(dto.code());
        return ResponseEntity.ok().build();
    }
}