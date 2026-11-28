package com.alocanote.api.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ForgotPasswordRequestDTO(
        @NotBlank(message = "O e-mail é obrigatório.")
        @Email(message = "Digite um endereço de e-mail válido.")
        String email
) {
}
