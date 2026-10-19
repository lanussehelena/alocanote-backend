package com.alocanote.api.dto.request;

public record UpdateUserProfileRequestDTO(
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
) {}
