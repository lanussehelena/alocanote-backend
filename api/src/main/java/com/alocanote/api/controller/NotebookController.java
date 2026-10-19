package com.alocanote.api.controller;

import com.alocanote.api.dto.request.CreateNotebookRequestDTO;
import com.alocanote.api.dto.response.NotebookResponseDTO;
import com.alocanote.api.service.NotebookService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({"/api/notebooks", "/notebooks"})
public class NotebookController {

    private final NotebookService notebookService;

    public NotebookController(NotebookService notebookService) {
        this.notebookService = notebookService;
    }

    public ResponseEntity<NotebookResponseDTO> criarNotebook(CreateNotebookRequestDTO dto) {
        return criarNotebook(dto, null);
    }

    @PostMapping
    public ResponseEntity<NotebookResponseDTO> criarNotebook(
            @Valid @RequestBody CreateNotebookRequestDTO dto,
            @AuthenticationPrincipal UserDetails userDetails) {
        String email = (userDetails != null) ? userDetails.getUsername() : null;
        NotebookResponseDTO novoNotebook = (email != null)
                ? notebookService.createNotebook(dto, email)
                : notebookService.createNotebook(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(novoNotebook);
    }

    @GetMapping
    public ResponseEntity<List<NotebookResponseDTO>> listarTodos() {
        List<NotebookResponseDTO> notebooks = notebookService.findAllDTOs();
        return ResponseEntity.ok(notebooks);
    }

    @GetMapping("/{id}")
    public ResponseEntity<NotebookResponseDTO> buscarPorId(@PathVariable Long id) {
        NotebookResponseDTO notebook = notebookService.findDtoById(id);
        return ResponseEntity.ok(notebook);
    }

    public ResponseEntity<Void> excluirNotebook(Long id) {
        return excluirNotebook(id, null);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluirNotebook(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        String email = (userDetails != null) ? userDetails.getUsername() : null;
        if (email != null) {
            notebookService.deleteNotebook(id, email);
        } else {
            notebookService.deleteNotebook(id);
        }
        return ResponseEntity.noContent().build();
    }
}
