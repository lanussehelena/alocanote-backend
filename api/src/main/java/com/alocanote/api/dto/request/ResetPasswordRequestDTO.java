package com.alocanote.api.dto.request;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ResetPasswordRequestDTO(
        @JsonAlias({"token", "firebaseToken", "accessToken"})
        @NotBlank(message = "O token de autenticação do Firebase é obrigatório.")
        String idToken,

        @NotBlank(message = "A nova senha é obrigatória.")
        @Size(min = 6, message = "A nova senha deve ter pelo menos 6 caracteres.")
        String newPassword
) {
}
