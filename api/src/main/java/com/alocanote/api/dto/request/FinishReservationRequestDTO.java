package com.alocanote.api.dto.request;

public record FinishReservationRequestDTO(
        String locationType,
        String address,
        Double latitude,
        Double longitude,
        String photoUrl
) {
}
