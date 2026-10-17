package com.alocanote.api.controller;

import com.alocanote.api.dto.request.CreateNotebookRequestDTO;
import com.alocanote.api.dto.response.NotebookResponseDTO;
import com.alocanote.api.service.NotebookService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notebooks")
public class NotebookController {

    private final NotebookService notebookService;

    public NotebookController(NotebookService notebookService) {
        this.notebookService = notebookService;
    }

    @PostMapping
    public ResponseEntity<NotebookResponseDTO> criarNotebook(@Valid @RequestBody CreateNotebookRequestDTO dto) {
        NotebookResponseDTO novoNotebook = notebookService.createNotebook(dto);
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
}