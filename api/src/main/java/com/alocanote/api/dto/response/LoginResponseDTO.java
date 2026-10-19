package com.alocanote.api.dto.response;

import lombok.Builder;

@Builder
public record LoginResponseDTO(
        String token,
        String type,
        String email,
        String name,
        String role
) {
    public LoginResponseDTO(String token, String email, String name, String role) {
        this(token, "Bearer", email, name, role);
    }

    // Métodos de conveniência para compatibilidade com chamadas estilo JavaBean
    public String getToken() { return token; }
    public String getType() { return type; }
    public String getEmail() { return email; }
    public String getName() { return name; }
    public String getRole() { return role; }
}
