package com.alocanote.api.controller;

import com.alocanote.api.dto.request.RegisterUserRequestDTO;
import com.alocanote.api.dto.request.UpdateUserProfileRequestDTO;
import com.alocanote.api.dto.response.UserResponseDTO;
import com.alocanote.api.model.enums.Role;
import com.alocanote.api.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    private final UserDetails userDetails = new User("helena@alocanote.com", "password", Collections.emptyList());

    @Test
    @DisplayName("Deve cadastrar novo usuário retornando UserResponseDTO com HTTP 201")
    void shouldRegisterUserSuccessfully() {
        RegisterUserRequestDTO request = new RegisterUserRequestDTO(
                "Carlos Souza", "carlos@alocanote.com", "senha123", "83977776666", Role.PROJETISTA, null
        );

        UserResponseDTO responseDto = new UserResponseDTO(
                1L, "Carlos Souza", "carlos@alocanote.com", "83977776666",
                Role.PROJETISTA, null, "ROLE_USER", null,
                null, null, null, null, null, null, LocalDateTime.now()
        );

        when(userService.register(request)).thenReturn(responseDto);

        ResponseEntity<UserResponseDTO> response = userController.register(request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Carlos Souza", response.getBody().name());
        assertEquals(Role.PROJETISTA, response.getBody().role());
        verify(userService).register(request);
    }

    @Test
    @DisplayName("Deve listar todos os usuários retornando lista de UserResponseDTO")
    void shouldListAllUsers() {
        UserResponseDTO u1 = new UserResponseDTO(1L, "Ana", "ana@alocanote.com", "83911", Role.CONFERENTE, null, "ROLE_USER", null, null, null, null, null, null, null, LocalDateTime.now());
        UserResponseDTO u2 = new UserResponseDTO(2L, "Bruno", "bruno@alocanote.com", "83922", Role.CONSULTOR_VENDAS, null, "ROLE_USER", null, null, null, null, null, null, null, LocalDateTime.now());

        when(userService.findAllDTOs()).thenReturn(List.of(u1, u2));

        ResponseEntity<List<UserResponseDTO>> response = userController.listarTodos();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        assertEquals("Ana", response.getBody().get(0).name());
    }

    @Test
    @DisplayName("Deve retornar o perfil do usuário autenticado via GET /users/me")
    void shouldGetCurrentUserProfile() {
        UserResponseDTO profileDto = new UserResponseDTO(
                1L, "Helena", "helena@alocanote.com", "5511999999999",
                Role.PROJETISTA, null, "ROLE_USER", "https://avatar.url/helena.png",
                null, null, null, null, null, null, LocalDateTime.now()
        );

        when(userService.getCurrentUser("helena@alocanote.com")).thenReturn(profileDto);

        ResponseEntity<UserResponseDTO> response = userController.obterMeuPerfil(userDetails);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Helena", response.getBody().name());
        assertEquals("https://avatar.url/helena.png", response.getBody().profilePictureUrl());
        verify(userService).getCurrentUser("helena@alocanote.com");
    }

    @Test
    @DisplayName("Deve atualizar o perfil do usuário autenticado via PUT /users/me")
    void shouldUpdateCurrentUserProfile() {
        UpdateUserProfileRequestDTO updateRequest = new UpdateUserProfileRequestDTO(
                "Helena Atualizada", "5511988887777", "https://avatar.url/novo.png",
                null, "Rua A", "100", "Centro", "João Pessoa", "PB", "58000-000"
        );

        UserResponseDTO updatedDto = new UserResponseDTO(
                1L, "Helena Atualizada", "helena@alocanote.com", "5511988887777",
                Role.PROJETISTA, null, "ROLE_USER", "https://avatar.url/novo.png",
                "Rua A", "100", "Centro", "João Pessoa", "PB", "58000-000", LocalDateTime.now()
        );

        when(userService.updateCurrentUser("helena@alocanote.com", updateRequest)).thenReturn(updatedDto);

        ResponseEntity<UserResponseDTO> response = userController.atualizarMeuPerfil(updateRequest, userDetails);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Helena Atualizada", response.getBody().name());
        assertEquals("5511988887777", response.getBody().phone());
        assertEquals("https://avatar.url/novo.png", response.getBody().profilePictureUrl());
        verify(userService).updateCurrentUser("helena@alocanote.com", updateRequest);
    }
}
