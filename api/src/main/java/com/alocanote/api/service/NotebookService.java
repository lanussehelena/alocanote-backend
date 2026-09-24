package com.alocanote.api.service;

import com.alocanote.api.model.entity.Notebook;
import com.alocanote.api.model.enums.NotebookStatus;
import com.alocanote.api.repository.NotebookRepository;
import com.alocanote.api.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotebookService {

    private final NotebookRepository notebookRepository;

    public NotebookService(NotebookRepository notebookRepository) {
        this.notebookRepository = notebookRepository;
    }

    public List<Notebook> findAll() {
        return notebookRepository.findAll();
    }

    public Notebook findById(Long id) {
        return notebookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notebook não encontrado com o ID: " + id));
    }

    public void updateStatus(Long notebookId, NotebookStatus status) {
        Notebook notebook = findById(notebookId);
        notebook.setStatus(status);
        notebookRepository.save(notebook);
    }
}