package com.alocanote.api.controller;

import com.alocanote.api.model.entity.Notebook;
import com.alocanote.api.service.NotebookService;
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

    @GetMapping
    public ResponseEntity<List<Notebook>> listarTodos() {
        List<Notebook> notebooks = notebookService.findAll();
        return ResponseEntity.ok(notebooks);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Notebook> buscarPorId(@PathVariable Long id) {
        Notebook notebook = notebookService.findById(id);
        return ResponseEntity.ok(notebook);
    }
}