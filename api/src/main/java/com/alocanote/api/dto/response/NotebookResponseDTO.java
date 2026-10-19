package com.alocanote.api.dto.response;

import com.alocanote.api.model.entity.Notebook;
import com.alocanote.api.model.enums.NotebookStatus;

public record NotebookResponseDTO(
        Long id,
        String name,
        String modelDescription,
        NotebookStatus status
) {
    public static NotebookResponseDTO fromEntity(Notebook notebook) {
        if (notebook == null) return null;
        return new NotebookResponseDTO(
                notebook.getId(),
                notebook.getName(),
                notebook.getModelDescription(),
                notebook.getStatus()
        );
    }
}