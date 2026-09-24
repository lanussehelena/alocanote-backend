package com.alocanote.api.controller;

import com.alocanote.api.model.entity.Reservation;
import com.alocanote.api.service.ReservationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reservations")
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @PostMapping("/agendar")
    public ResponseEntity<Reservation> agendarNotebook(
            @RequestParam Long notebookId,
            @RequestParam Long userId) {

        Reservation novaReserva = reservationService.createReservation(notebookId, userId);

        return ResponseEntity.status(HttpStatus.CREATED).body(novaReserva);
    }
}