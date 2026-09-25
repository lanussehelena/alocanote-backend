package com.alocanote.api.dto.response;

import com.alocanote.api.model.enums.Role;

public record AuthResponseDTO(
        String token,
        String tokenType,
        Long userId,
        String name,
        String email,
        Role role
) {
    public AuthResponseDTO(String token, Long userId, String name, String email, Role role) {
        this(token, "Bearer", userId, name, email, role);
    }
}