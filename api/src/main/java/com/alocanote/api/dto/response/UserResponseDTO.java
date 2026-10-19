package com.alocanote.api.dto.response;

import com.alocanote.api.model.entity.User;
import com.alocanote.api.model.enums.Role;

import java.time.LocalDateTime;

public record UserResponseDTO(
        Long id,
        String name,
        String email,
        String phone,
        Role role,
        String customRole,
        String accessLevel,
        String profilePictureUrl,
        String street,
        String number,
        String neighborhood,
        String city,
        String state,
        String zipCode,
        LocalDateTime createdAt
) {
    public static UserResponseDTO fromEntity(User user) {
        if (user == null) return null;
        return new UserResponseDTO(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getPhone(),
                user.getRole(),
                user.getCustomRole(),
                user.getAccessLevel(),
                user.getProfilePictureUrl(),
                user.getStreet(),
                user.getNumber(),
                user.getNeighborhood(),
                user.getCity(),
                user.getState(),
                user.getZipCode(),
                user.getCreatedAt()
        );
    }
}
