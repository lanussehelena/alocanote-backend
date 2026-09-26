package com.alocanote.api.controller;

import com.alocanote.api.dto.request.LoginRequestDTO;
import com.alocanote.api.dto.response.LoginResponseDTO;
import com.alocanote.api.exception.BusinessException;
import com.alocanote.api.model.entity.User;
import com.alocanote.api.model.enums.Role;
import com.alocanote.api.repository.UserRepository;
import com.alocanote.api.security.JwtTokenProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private AuthController authController;

    @Test
    @DisplayName("Deve realizar login com sucesso e retornar token JWT")
    void shouldLoginSuccessfully() {
        String email = "maria@alocanote.com";
        LoginRequestDTO request = new LoginRequestDTO();
        request.setEmail(email);
        request.setPassword("123456");

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
        assertEquals("jwt.token.mock", response.getBody().getToken());
        assertEquals("Bearer", response.getBody().getType());
        assertEquals(email, response.getBody().getEmail());
    }

    @Test
    @DisplayName("Deve lançar BusinessException quando o e-mail não existir")
    void shouldThrowExceptionWhenUserNotFound() {
        String email = "naoexiste@alocanote.com";
        LoginRequestDTO request = new LoginRequestDTO();
        request.setEmail(email);
        request.setPassword("123456");

        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        assertThrows(BusinessException.class, () -> {
            authController.login(request);
        });
    }
}
