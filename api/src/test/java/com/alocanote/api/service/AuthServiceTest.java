package com.alocanote.api.service;

import com.alocanote.api.dto.request.FirebaseAuthRequestDTO;
import com.alocanote.api.dto.request.ResetPasswordRequestDTO;
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

    @Test
    @DisplayName("Deve bloquear autenticação via Firebase quando o e-mail ainda não foi verificado")
    void shouldRejectFirebaseLoginWhenEmailIsNotVerified() {
        String unverifiedToken = JWT.create()
                .withSubject("firebase-uid-unverified")
                .withClaim("email", "pendente@alocanote.com")
                .withClaim("name", "Colaborador Pendente")
                .withClaim("email_verified", false)
                .withExpiresAt(Instant.now().plusSeconds(3600))
                .sign(Algorithm.HMAC256("test-secret"));

        BusinessException exception = assertThrows(BusinessException.class, () ->
                authService.authenticateWithFirebase(new FirebaseAuthRequestDTO(unverifiedToken))
        );

        assertEquals(AuthService.EMAIL_NOT_VERIFIED_MESSAGE, exception.getMessage());
        verify(jwtTokenProvider, never()).generateToken(any(User.class));
    }

    @Test
    @DisplayName("Deve sincronizar senha no banco ao autenticar via Firebase com e-mail e senha")
    void shouldSyncPasswordWhenAuthenticatingWithFirebaseEmail() {
        String idToken = JWT.create()
                .withSubject("firebase-uid-sync")
                .withClaim("email", "sync@alocanote.com")
                .withClaim("name", "Colaborador Sync")
                .withClaim("email_verified", true)
                .withClaim("firebase", Map.of("sign_in_provider", "password"))
                .withExpiresAt(Instant.now().plusSeconds(3600))
                .sign(Algorithm.HMAC256("test-secret"));

        User existing = User.builder()
                .id(8L)
                .name("Colaborador Sync")
                .email("sync@alocanote.com")
                .password("$2a$10$senhaAntiga")
                .role(Role.PROJETISTA)
                .accessLevel("ROLE_USER")
                .emailVerified(true)
                .build();

        FirebaseAuthRequestDTO request = new FirebaseAuthRequestDTO(
                idToken, "Colaborador Sync", "sync@alocanote.com", null, null, null, null, "novaSenha123"
        );

        when(userRepository.findByFirebaseUid("firebase-uid-sync")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("sync@alocanote.com")).thenReturn(Optional.of(existing));
        when(passwordEncoder.matches("novaSenha123", "$2a$10$senhaAntiga")).thenReturn(false);
        when(passwordEncoder.encode("novaSenha123")).thenReturn("$2a$10$senhaNova");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        when(jwtTokenProvider.generateToken(existing)).thenReturn("jwt.sync.token");

        LoginResponseDTO response = authService.authenticateWithFirebase(request);

        assertEquals("jwt.sync.token", response.token());
        assertEquals("$2a$10$senhaNova", existing.getPassword());
        verify(passwordEncoder).encode("novaSenha123");
    }

    @Test
    @DisplayName("Deve validar conta existente ao preparar recuperação de senha")
    void shouldPreparePasswordResetForExistingUser() {
        User existing = User.builder()
                .id(9L)
                .name("Maria")
                .email("maria@alocanote.com")
                .role(Role.PROJETISTA)
                .accessLevel("ROLE_USER")
                .build();

        when(userRepository.findByEmail("maria@alocanote.com")).thenReturn(Optional.of(existing));

        Map<String, String> result = authService.preparePasswordReset("  MARIA@alocanote.com ");

        assertEquals("maria@alocanote.com", result.get("email"));
        assertNotNull(result.get("message"));
    }

    @Test
    @DisplayName("Deve lançar BusinessException ao solicitar recuperação para e-mail inexistente")
    void shouldRejectForgotPasswordWhenEmailNotFound() {
        when(userRepository.findByEmail("desconhecido@alocanote.com")).thenReturn(Optional.empty());
        when(userRepository.findByEmailIgnoreCase("desconhecido@alocanote.com")).thenReturn(Optional.empty());

        BusinessException exception = assertThrows(BusinessException.class, () ->
                authService.preparePasswordReset("desconhecido@alocanote.com")
        );

        assertTrue(exception.getMessage().contains("Nenhuma conta encontrada"));
    }

    @Test
    @DisplayName("Deve sincronizar nova senha após redefinição usando token válido do Firebase")
    void shouldSyncResetPasswordWithValidFirebaseToken() {
        String idToken = JWT.create()
                .withSubject("firebase-uid-reset")
                .withClaim("email", "reset@alocanote.com")
                .withClaim("email_verified", true)
                .withExpiresAt(Instant.now().plusSeconds(3600))
                .sign(Algorithm.HMAC256("test-secret"));

        User existing = User.builder()
                .id(12L)
                .name("Colaborador Reset")
                .email("reset@alocanote.com")
                .password("$2a$10$antiga")
                .role(Role.PROJETISTA)
                .accessLevel("ROLE_USER")
                .emailVerified(false)
                .build();

        when(userRepository.findByFirebaseUid("firebase-uid-reset")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("reset@alocanote.com")).thenReturn(Optional.of(existing));
        when(passwordEncoder.encode("novaSenha456")).thenReturn("$2a$10$novaSenha456");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        Map<String, String> result = authService.syncResetPassword(
                new ResetPasswordRequestDTO(idToken, "novaSenha456")
        );

        assertEquals("reset@alocanote.com", result.get("email"));
        assertEquals("$2a$10$novaSenha456", existing.getPassword());
        assertEquals("firebase-uid-reset", existing.getFirebaseUid());
        assertTrue(existing.getEmailVerified());
        verify(userRepository).save(existing);
    }
}
