package com.alocanote.api.controller;

import com.alocanote.api.dto.request.CreateNotebookRequestDTO;
import com.alocanote.api.dto.response.NotebookResponseDTO;
import com.alocanote.api.model.enums.NotebookStatus;
import com.alocanote.api.service.NotebookService;
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

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotebookControllerTest {

    @Mock
    private NotebookService notebookService;

    @InjectMocks
    private NotebookController notebookController;

    private final UserDetails adminDetails = new User("admin@alocanote.com", "password", Collections.emptyList());

    @Test
    @DisplayName("Deve criar notebook e retornar NotebookResponseDTO com HTTP 201")
    void shouldCreateNotebookSuccessfully() {
        CreateNotebookRequestDTO request = new CreateNotebookRequestDTO(
                "Notebook 01", "Dell Inspiron 15", NotebookStatus.DISPONIVEL
        );
        NotebookResponseDTO responseDto = new NotebookResponseDTO(
                1L, "Notebook 01", "Dell Inspiron 15", NotebookStatus.DISPONIVEL
        );

        when(notebookService.createNotebook(request)).thenReturn(responseDto);

        ResponseEntity<NotebookResponseDTO> response = notebookController.criarNotebook(request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().id());
        assertEquals("Notebook 01", response.getBody().name());
        verify(notebookService).createNotebook(request);
    }

    @Test
    @DisplayName("Deve listar todos os notebooks retornando lista de DTOs")
    void shouldListAllNotebooks() {
        NotebookResponseDTO dto1 = new NotebookResponseDTO(1L, "Note 1", "i5", NotebookStatus.DISPONIVEL);
        NotebookResponseDTO dto2 = new NotebookResponseDTO(2L, "Note 2", "i7", NotebookStatus.EM_USO);

        when(notebookService.findAllDTOs()).thenReturn(List.of(dto1, dto2));

        ResponseEntity<List<NotebookResponseDTO>> response = notebookController.listarTodos();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        assertEquals("Note 1", response.getBody().get(0).name());
    }

    @Test
    @DisplayName("Deve buscar notebook por ID retornando DTO")
    void shouldFindNotebookById() {
        NotebookResponseDTO dto = new NotebookResponseDTO(10L, "Note 10", "i7", NotebookStatus.DISPONIVEL);

        when(notebookService.findDtoById(10L)).thenReturn(dto);

        ResponseEntity<NotebookResponseDTO> response = notebookController.buscarPorId(10L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(10L, response.getBody().id());
    }

    @Test
    @DisplayName("Deve excluir notebook como administrador retornando HTTP 204")
    void shouldDeleteNotebookSuccessfully() {
        doNothing().when(notebookService).deleteNotebook(10L, "admin@alocanote.com");

        ResponseEntity<Void> response = notebookController.excluirNotebook(10L, adminDetails);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(notebookService).deleteNotebook(10L, "admin@alocanote.com");
    }
}
