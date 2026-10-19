package com.alocanote.api.dto.request;

import com.alocanote.api.model.enums.NotebookStatus;
import jakarta.validation.constraints.NotBlank;

public record CreateNotebookRequestDTO(
        @NotBlank(message = "O nome do notebook é obrigatório")
        String name,

        @NotBlank(message = "A descrição do modelo é obrigatória")
        String modelDescription,

        NotebookStatus status
) {}
