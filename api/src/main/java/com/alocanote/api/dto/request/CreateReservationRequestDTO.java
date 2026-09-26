package com.alocanote.api.dto.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record CreateReservationRequestDTO(
        @NotNull(message = "O ID do notebook é obrigatório.")
        Long notebookId,

        @NotNull(message = "A data/hora de início é obrigatória.")
        @FutureOrPresent(message = "A data de início deve ser no presente ou futuro.")
        LocalDateTime startTime,

        @NotNull(message = "A data/hora de término é obrigatória.")
        @Future(message = "A data de devolução deve ser no futuro.")
        LocalDateTime endTime,

        @NotNull(message = "O ID do local de retirada é obrigatório.")
        Long locationId,

        String purpose // Finalidade/Justificativa do uso (ex: Medição de obra)
) {}