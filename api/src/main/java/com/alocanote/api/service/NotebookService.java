package com.alocanote.api.service;

import com.alocanote.api.dto.request.CreateNotebookRequestDTO;
import com.alocanote.api.dto.response.NotebookResponseDTO;
import com.alocanote.api.exception.ResourceNotFoundException;
import com.alocanote.api.model.entity.Notebook;
import com.alocanote.api.model.enums.NotebookStatus;
import com.alocanote.api.repository.NotebookRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class NotebookService {

    private final NotebookRepository notebookRepository;

    public NotebookService(NotebookRepository notebookRepository) {
        this.notebookRepository = notebookRepository;
    }

    @Transactional(readOnly = true)
    public List<NotebookResponseDTO> findAllDTOs() {
        return notebookRepository.findAll().stream()
                .map(NotebookResponseDTO::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<Notebook> findAll() {
        return notebookRepository.findAll();
    }

    @Transactional
    public NotebookResponseDTO createNotebook(CreateNotebookRequestDTO dto) {
        Notebook notebook = Notebook.builder()
                .name(dto.name())
                .modelDescription(dto.modelDescription())
                .status(dto.status() != null ? dto.status() : NotebookStatus.DISPONIVEL)
                .build();

        Notebook saved = notebookRepository.save(notebook);
        return NotebookResponseDTO.fromEntity(saved);
    }

    @Transactional
    public Notebook createNotebook(Notebook notebook) {
        return notebookRepository.save(notebook);
    }

    @Transactional(readOnly = true)
    public Notebook findById(Long id) {
        return notebookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notebook não encontrado com o ID: " + id));
    }

    @Transactional(readOnly = true)
    public NotebookResponseDTO findDtoById(Long id) {
        return NotebookResponseDTO.fromEntity(findById(id));
    }

    @Transactional
    public void updateStatus(Long notebookId, NotebookStatus status) {
        Notebook notebook = findById(notebookId);
        notebook.setStatus(status);
        notebookRepository.save(notebook);
    }
}