package com.alocanote.api.dto.response;

import com.alocanote.api.model.entity.Reservation;
import com.alocanote.api.model.enums.ReservationStatus;

import java.time.LocalDateTime;

public record ReservationResponseDTO(
        Long id,
        Long userId,
        String userName,
        Long notebookId,
        String notebookName,
        String notebookModel,
        LocalDateTime startTime,
        LocalDateTime endTime,
        Long locationId,
        String locationName,
        ReservationStatus status,
        String purpose
) {
    public static ReservationResponseDTO fromEntity(Reservation reservation) {
        if (reservation == null) return null;
        return new ReservationResponseDTO(
                reservation.getId(),
                reservation.getUser() != null ? reservation.getUser().getId() : null,
                reservation.getUser() != null ? reservation.getUser().getName() : null,
                reservation.getNotebook() != null ? reservation.getNotebook().getId() : null,
                reservation.getNotebook() != null ? reservation.getNotebook().getName() : null,
                reservation.getNotebook() != null ? reservation.getNotebook().getModelDescription() : null,
                reservation.getDepartureDateTime(),
                reservation.getReturnDateTime(),
                reservation.getLocation() != null ? reservation.getLocation().getId() : null,
                reservation.getLocation() != null ? reservation.getLocation().getName() : null,
                reservation.getStatus(),
                reservation.getPurpose()
        );
    }
}