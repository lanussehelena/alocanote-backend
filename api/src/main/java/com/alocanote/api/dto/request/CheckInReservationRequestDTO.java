package com.alocanote.api.dto.request;

public record CheckInReservationRequestDTO(
        String locationType,
        String address,
        Double latitude,
        Double longitude
) {
}
