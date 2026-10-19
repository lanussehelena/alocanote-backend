package com.alocanote.api.service;

import com.alocanote.api.dto.request.FirebaseAuthRequestDTO;
import com.alocanote.api.dto.response.LoginResponseDTO;
import com.alocanote.api.exception.BusinessException;
import com.alocanote.api.model.entity.User;
import com.alocanote.api.model.enums.Role;
import com.alocanote.api.repository.UserRepository;
import com.alocanote.api.security.JwtTokenProvider;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    @Test
    @DisplayName("Deve criar novo usuário automaticamente no primeiro acesso com Conta Google via Firebase")
    void shouldCreateNewUserOnFirstGoogleLogin() {
        String idToken = JWT.create()
                .withSubject("firebase-uid-123")
                .withClaim("email", "novo.colaborador@gmail.com")
                .withClaim("name", "Novo Colaborador")
                .withClaim("picture", "https://lh3.googleusercontent.com/avatar.png")
                .withClaim("email_verified", true)
                .withClaim("firebase", Map.of("sign_in_provider", "google.com"))
                .withExpiresAt(Instant.now().plusSeconds(3600))
                .sign(Algorithm.HMAC256("test-secret"));

        FirebaseAuthRequestDTO request = new FirebaseAuthRequestDTO(idToken);

        when(userRepository.findByFirebaseUid("firebase-uid-123")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("novo.colaborador@gmail.com")).thenReturn(Optional.empty());
        when(userRepository.findByEmailIgnoreCase("novo.colaborador@gmail.com")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User saved = invocation.getArgument(0);
            saved.setId(10L);
            return saved;
        });
        when(jwtTokenProvider.generateToken(any(User.class))).thenReturn("backend.jwt.token");

        LoginResponseDTO response = authService.authenticateWithFirebase(request);

        assertNotNull(response);
        assertEquals("backend.jwt.token", response.token());
        assertEquals("novo.colaborador@gmail.com", response.email());
        assertEquals("Novo Colaborador", response.name());
        assertEquals("PROJETISTA", response.role());
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Deve vincular firebaseUid a usuário existente pelo e-mail e retornar token JWT")
    void shouldLinkFirebaseUidToExistingUserByEmail() {
        String idToken = JWT.create()
                .withSubject("firebase-uid-456")
                .withClaim("email", "existente@alocanote.com")
                .withClaim("name", "Colaborador Existente")
                .withClaim("email_verified", true)
                .withExpiresAt(Instant.now().plusSeconds(3600))
                .sign(Algorithm.HMAC256("test-secret"));

        User existing = User.builder()
                .id(5L)
                .name("Colaborador Existente")
                .email("existente@alocanote.com")
                .role(Role.CONFERENTE)
                .accessLevel("ROLE_USER")
                .build();

        when(userRepository.findByFirebaseUid("firebase-uid-456")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("existente@alocanote.com")).thenReturn(Optional.of(existing));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        when(jwtTokenProvider.generateToken(existing)).thenReturn("jwt.existing.token");

        LoginResponseDTO response = authService.authenticateWithFirebase(new FirebaseAuthRequestDTO(idToken));

        assertEquals("jwt.existing.token", response.token());
        assertEquals("CONFERENTE", response.role());
        assertEquals("firebase-uid-456", existing.getFirebaseUid());
        assertTrue(existing.getEmailVerified());
    }

    @Test
    @DisplayName("Deve rejeitar token do Firebase expirado")
    void shouldRejectExpiredFirebaseToken() {
        String expiredToken = JWT.create()
                .withSubject("firebase-uid-789")
                .withClaim("email", "expirado@alocanote.com")
                .withExpiresAt(Instant.now().minusSeconds(60))
                .sign(Algorithm.HMAC256("test-secret"));

        assertThrows(BusinessException.class, () ->
                authService.authenticateWithFirebase(new FirebaseAuthRequestDTO(expiredToken))
        );
    }
}
