package com.alocanote.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TokenValidationRequestDTO(
        @NotBlank(message = "O número de telefone é obrigatório.")
        String phone,

        @NotBlank(message = "O código de verificação é obrigatório.")
        @Size(min = 6, max = 6, message = "O código deve conter exatamente 6 dígitos.")
        String code
) {}