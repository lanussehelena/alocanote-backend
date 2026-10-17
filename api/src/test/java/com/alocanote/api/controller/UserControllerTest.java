package com.alocanote.api.controller;

import com.alocanote.api.dto.request.RegisterUserRequestDTO;
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

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

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
}
