package com.alocanote.api.dto.request;

import com.alocanote.api.model.enums.Role;

public record UpdateUserProfileRequestDTO(
        String name,
        String phone,
        String profilePictureUrl,
        Role role,
        String customRole,
        String street,
        String number,
        String neighborhood,
        String city,
        String state,
        String zipCode,
        String currentPassword,
        String newPassword
) {
    public UpdateUserProfileRequestDTO(
            String name,
            String phone,
            String profilePictureUrl,
            String customRole,
            String street,
            String number,
            String neighborhood,
            String city,
            String state,
            String zipCode
    ) {
        this(name, phone, profilePictureUrl, null, customRole, street, number, neighborhood, city, state, zipCode, null, null);
    }
}
