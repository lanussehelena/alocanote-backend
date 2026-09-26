package com.alocanote.api.dto.response;

import com.alocanote.api.model.enums.ReservationStatus;

import java.time.LocalDateTime;

public record ReservationResponseDTO(
        Long id,
        Long userId,
        String userName,
        Long notebookId,
        String notebookModel,
        String assetTag,
        LocalDateTime startTime,
        LocalDateTime endTime,
        String locationName,
        ReservationStatus status,
        String purpose
) {}