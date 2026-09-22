package com.alocanote.api.model.enums;

public enum Role {
    ADMINISTRADOR("Administrador"),
    PROJETISTA("Projetista"),
    CONFERENTE("Conferente"),
    CONSULTOR_VENDAS("Consultor de Vendas"),
    OUTROS("Outros");

    private final String description;

    Role(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}