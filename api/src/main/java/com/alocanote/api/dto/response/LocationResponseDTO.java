package com.alocanote.api.dto.response;

import com.alocanote.api.model.entity.Location;

public record LocationResponseDTO(
        Long id,
        String name,
        String address,
        String cep
) {
    public static LocationResponseDTO fromEntity(Location location) {
        if (location == null) return null;
        return new LocationResponseDTO(
                location.getId(),
                location.getName(),
                location.getAddress(),
                location.getCep()
        );
    }
}
