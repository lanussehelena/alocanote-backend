package com.alocanote.api.service;

import com.alocanote.api.dto.request.RegisterUserRequestDTO;
import com.alocanote.api.dto.request.UpdateUserProfileRequestDTO;
import com.alocanote.api.dto.response.UserResponseDTO;
import com.alocanote.api.exception.BusinessException;
import com.alocanote.api.model.entity.User;
import com.alocanote.api.model.enums.Role;
import com.alocanote.api.repository.ReservationRepository;
import com.alocanote.api.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private NotebookService notebookService;

    @InjectMocks
    private UserService userService;

    @Test
    @DisplayName("Deve permitir cadastro com papéis comuns (PROJETISTA, CONFERENTE, CONSULTOR_VENDAS, OUTROS) com ROLE_USER")
    void shouldRegisterWithAllowedRoles() {
        RegisterUserRequestDTO dto = new RegisterUserRequestDTO(
                "João Santos", "joao@alocanote.com", "senha123", "83988887777", Role.CONSULTOR_VENDAS, null
        );

        when(userRepository.existsByEmail("joao@alocanote.com")).thenReturn(false);
        when(userRepository.existsByEmailIgnoreCase("joao@alocanote.com")).thenReturn(false);
        when(userRepository.existsByPhone("83988887777")).thenReturn(false);
        when(passwordEncoder.encode("senha123")).thenReturn("encoded-pass");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(10L);
            return u;
        });

        UserResponseDTO response = userService.register(dto);

        assertNotNull(response);
        assertEquals(Role.CONSULTOR_VENDAS, response.role());
        assertEquals("ROLE_USER", response.accessLevel());
    }

    @Test
    @DisplayName("Deve rejeitar tentativa de auto-cadastro como ADMINISTRADOR")
    void shouldRejectRegistrationAsAdmin() {
        RegisterUserRequestDTO dto = new RegisterUserRequestDTO(
                "Hacker", "hacker@alocanote.com", "senha123", "83999998888", Role.ADMINISTRADOR, null
        );

        BusinessException exception = assertThrows(BusinessException.class, () -> userService.register(dto));
        assertTrue(exception.getMessage().contains("Não é permitido cadastrar-se como Administrador"));
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve rejeitar tentativa de usuário comum alterar próprio perfil para ADMINISTRADOR")
    void shouldRejectProfileUpdateToAdmin() {
        User user = User.builder()
                .id(1L)
                .name("Maria Silva")
                .email("maria@alocanote.com")
                .role(Role.PROJETISTA)
                .accessLevel("ROLE_USER")
                .build();

        UpdateUserProfileRequestDTO dto = new UpdateUserProfileRequestDTO(
                "Maria Hacker", null, null, Role.ADMINISTRADOR, null, null, null, null, null, null, null, null, null
        );

        when(userRepository.findByEmail("maria@alocanote.com")).thenReturn(Optional.of(user));

        BusinessException exception = assertThrows(BusinessException.class, () ->
                userService.updateCurrentUser("maria@alocanote.com", dto)
        );

        assertTrue(exception.getMessage().contains("Não é permitido alterar seu perfil para Administrador"));
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve permitir alterar próprio perfil para outro papel permitido (ex: CONFERENTE)")
    void shouldAllowProfileUpdateToAllowedRole() {
        User user = User.builder()
                .id(1L)
                .name("Maria Silva")
                .email("maria@alocanote.com")
                .role(Role.PROJETISTA)
                .accessLevel("ROLE_USER")
                .build();

        UpdateUserProfileRequestDTO dto = new UpdateUserProfileRequestDTO(
                "Maria Atualizada", null, null, Role.CONFERENTE, null, null, null, null, null, null, null, null, null
        );

        when(userRepository.findByEmail("maria@alocanote.com")).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        UserResponseDTO response = userService.updateCurrentUser("maria@alocanote.com", dto);

        assertNotNull(response);
        assertEquals(Role.CONFERENTE, response.role());
        assertEquals("ROLE_USER", response.accessLevel());
    }
}
