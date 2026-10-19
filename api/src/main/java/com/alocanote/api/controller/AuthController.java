package com.alocanote.api.controller;

import com.alocanote.api.dto.request.LoginRequestDTO;
import com.alocanote.api.dto.request.RegisterUserRequestDTO;
import com.alocanote.api.dto.request.SendSmsRequestDTO;
import com.alocanote.api.dto.request.TokenValidationRequestDTO;
import com.alocanote.api.dto.response.LoginResponseDTO;
import com.alocanote.api.dto.response.UserResponseDTO;
import com.alocanote.api.exception.BusinessException;
import com.alocanote.api.model.entity.User;
import com.alocanote.api.repository.UserRepository;
import com.alocanote.api.security.JwtTokenProvider;
import com.alocanote.api.service.AuthService;
import com.alocanote.api.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping({"/api/auth", "/auth"})
public class AuthController {

    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthService authService;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    public AuthController(UserRepository userRepository, JwtTokenProvider jwtTokenProvider, AuthService authService) {
        this(userRepository, jwtTokenProvider, authService, null, null);
    }

    @Autowired
    public AuthController(UserRepository userRepository,
                          JwtTokenProvider jwtTokenProvider,
                          AuthService authService,
                          UserService userService,
                          PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.jwtTokenProvider = jwtTokenProvider;
        this.authService = authService;
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@Valid @RequestBody LoginRequestDTO request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new BusinessException("E-mail ou senha inválidos."));

        if (passwordEncoder != null && user.getPassword() != null
                && !passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new BusinessException("E-mail ou senha inválidos.");
        }

        String token = jwtTokenProvider.generateToken(user);

        String userRole = (user.getRole() != null) ? user.getRole().name() : user.getAccessLevel();

        LoginResponseDTO response = LoginResponseDTO.builder()
                .token(token)
                .type("Bearer")
                .email(user.getEmail())
                .name(user.getName())
                .role(userRole)
                .build();

        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponseDTO> register(@Valid @RequestBody RegisterUserRequestDTO requestDTO) {
        UserResponseDTO newUser = userService.register(requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(newUser);
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