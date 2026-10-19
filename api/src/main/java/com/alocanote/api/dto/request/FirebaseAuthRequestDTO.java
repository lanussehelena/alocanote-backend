package com.alocanote.api.dto.request;

import com.alocanote.api.model.enums.Role;
import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotBlank;

public record FirebaseAuthRequestDTO(
        @JsonAlias({"token", "firebaseToken", "accessToken"})
        @NotBlank(message = "O token de autenticação do Firebase é obrigatório.")
        String idToken,

        String name,

        String email,

        String phone,

        @JsonAlias({"photoUrl", "picture", "avatarUrl"})
        String profilePictureUrl,

        Role role,

        String customRole
) {
    public FirebaseAuthRequestDTO(String idToken) {
        this(idToken, null, null, null, null, null, null);
    }
}
