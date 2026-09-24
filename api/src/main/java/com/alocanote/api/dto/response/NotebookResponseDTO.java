package com.alocanote.api.dto.response;

import com.alocanote.api.model.entity.Notebook;
import com.alocanote.api.model.enums.NotebookStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotebookResponseDTO {

    private Long id;
    private String name;
    private String modelDescription;
    private NotebookStatus status;


    public static NotebookResponseDTO fromEntity(Notebook notebook) {
        return NotebookResponseDTO.builder()
                .id(notebook.getId())
                .name(notebook.getName())
                .modelDescription(notebook.getModelDescription())
                .status(notebook.getStatus())
                .build();
    }
}