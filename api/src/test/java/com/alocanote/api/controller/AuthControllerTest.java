package com.alocanote.api.controller;

import com.alocanote.api.dto.request.LoginRequestDTO;
import com.alocanote.api.dto.request.RegisterUserRequestDTO;
import com.alocanote.api.dto.response.LoginResponseDTO;
import com.alocanote.api.dto.response.UserResponseDTO;
import com.alocanote.api.exception.BusinessException;
import com.alocanote.api.model.entity.User;
import com.alocanote.api.model.enums.Role;
import com.alocanote.api.repository.UserRepository;
import com.alocanote.api.security.JwtTokenProvider;
import com.alocanote.api.service.AuthService;
import com.alocanote.api.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private AuthService authService;

    @Mock
    private UserService userService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthController authController;

    @Test
    @DisplayName("Deve realizar login com sucesso e retornar token JWT")
    void shouldLoginSuccessfully() {
        String email = "maria@alocanote.com";
        LoginRequestDTO request = new LoginRequestDTO(email, "123456");

        User user = User.builder()
                .id(1L)
                .name("Maria Silva")
                .email(email)
                .role(Role.ADMINISTRADOR)
                .accessLevel("ROLE_ADMIN")
                .build();

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(jwtTokenProvider.generateToken(user)).thenReturn("jwt.token.mock");

        ResponseEntity<LoginResponseDTO> response = authController.login(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("jwt.token.mock", response.getBody().token());
        assertEquals("Bearer", response.getBody().type());
        assertEquals(email, response.getBody().email());
    }

    @Test
    @DisplayName("Deve lançar BusinessException quando o e-mail não existir")
    void shouldThrowExceptionWhenUserNotFound() {
        String email = "naoexiste@alocanote.com";
        LoginRequestDTO request = new LoginRequestDTO(email, "123456");

        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        assertThrows(BusinessException.class, () -> {
            authController.login(request);
        });
    }

    @Test
    @DisplayName("Deve lançar BusinessException quando a senha estiver incorreta")
    void shouldThrowExceptionWhenPasswordIsInvalid() {
        String email = "maria@alocanote.com";
        LoginRequestDTO request = new LoginRequestDTO(email, "senhaErrada");

        User user = User.builder()
                .id(1L)
                .name("Maria Silva")
                .email(email)
                .password("$2a$10$hash")
                .role(Role.PROJETISTA)
                .accessLevel("ROLE_USER")
                .build();

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("senhaErrada", "$2a$10$hash")).thenReturn(false);

        assertThrows(BusinessException.class, () -> authController.login(request));
    }

    @Test
    @DisplayName("Deve cadastrar novo usuário via POST /auth/register retornando HTTP 201")
    void shouldRegisterUserViaAuthController() {
        RegisterUserRequestDTO request = new RegisterUserRequestDTO(
                "Helena", "helena@alocanote.com", "123456", "5511999999999", Role.PROJETISTA, null
        );
        UserResponseDTO responseDto = new UserResponseDTO(
                1L, "Helena", "helena@alocanote.com", "5511999999999",
                Role.PROJETISTA, null, "ROLE_USER", null,
                null, null, null, null, null, null, LocalDateTime.now()
        );

        when(userService.register(request)).thenReturn(responseDto);

        ResponseEntity<UserResponseDTO> response = authController.register(request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Helena", response.getBody().name());
        verify(userService).register(request);
    }
}
