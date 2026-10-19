package com.alocanote.api.controller;

import com.alocanote.api.dto.request.FirebaseAuthRequestDTO;
import com.alocanote.api.dto.request.LoginRequestDTO;
import com.alocanote.api.dto.request.RegisterUserRequestDTO;
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
        String normalizedEmail = request.email().trim().toLowerCase();
        User user = userRepository.findByEmail(normalizedEmail)
                .or(() -> userRepository.findByEmailIgnoreCase(normalizedEmail))
                .orElseThrow(() -> new BusinessException("E-mail não encontrado. Verifique o endereço digitado."));

        if ((user.getPassword() == null || user.getPassword().isBlank())
                && "GOOGLE".equalsIgnoreCase(user.getAuthProvider())) {
            throw new BusinessException("Esta conta foi cadastrada sem senha. Acesse a tela de Cadastro com este mesmo e-mail para cadastrar sua senha.");
        }

        if (passwordEncoder != null && user.getPassword() != null && !user.getPassword().isBlank()
                && !passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new BusinessException("Senha incorreta. Tente novamente.");
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

    @PostMapping({"/firebase", "/google"})
    public ResponseEntity<LoginResponseDTO> authenticateWithFirebase(
            @Valid @RequestBody FirebaseAuthRequestDTO requestDTO) {
        LoginResponseDTO response = authService.authenticateWithFirebase(requestDTO);
        return ResponseEntity.ok(response);
    }
}