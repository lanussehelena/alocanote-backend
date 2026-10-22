package com.alocanote.api.controller;

import com.alocanote.api.dto.request.CheckInReservationRequestDTO;
import com.alocanote.api.dto.request.CreateReservationRequestDTO;
import com.alocanote.api.dto.request.FinishReservationRequestDTO;
import com.alocanote.api.dto.response.ReservationResponseDTO;
import com.alocanote.api.model.entity.Reservation;
import com.alocanote.api.service.ReservationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({"/api/reservations", "/reservations"})
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @PostMapping({"", "/agendar"})
    public ResponseEntity<ReservationResponseDTO> agendarNotebook(
            @Valid @RequestBody CreateReservationRequestDTO dto,
            @AuthenticationPrincipal UserDetails userDetails) {

        Reservation novaReserva = reservationService.createReservation(dto, extractUsername(userDetails));
        return ResponseEntity.status(HttpStatus.CREATED).body(ReservationResponseDTO.fromEntity(novaReserva));
    }

    @GetMapping
    public ResponseEntity<List<ReservationResponseDTO>> listarMinhasReservas(
            @AuthenticationPrincipal UserDetails userDetails) {

        List<Reservation> reservas = reservationService.findMyReservations(extractUsername(userDetails));
        List<ReservationResponseDTO> response = reservas.stream()
                .map(ReservationResponseDTO::fromEntity)
                .toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/all")
    public ResponseEntity<List<ReservationResponseDTO>> listarTodasReservas() {
        List<Reservation> reservas = reservationService.findAllReservations();
        List<ReservationResponseDTO> response = reservas.stream()
                .map(ReservationResponseDTO::fromEntity)
                .toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/calendar")
    public ResponseEntity<List<ReservationResponseDTO>> listarCalendario(
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year) {

        List<Reservation> reservas = reservationService.findCalendarReservations(month, year);
        List<ReservationResponseDTO> response = reservas.stream()
                .map(ReservationResponseDTO::fromEntity)
                .toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReservationResponseDTO> buscarPorId(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {

        Reservation reserva = reservationService.findById(id, extractUsername(userDetails));
        return ResponseEntity.ok(ReservationResponseDTO.fromEntity(reserva));
    }

    public ResponseEntity<ReservationResponseDTO> checkIn(
            Long id,
            UserDetails userDetails) {
        return checkIn(id, null, userDetails);
    }

    @PatchMapping("/{id}/check-in")
    public ResponseEntity<ReservationResponseDTO> checkIn(
            @PathVariable Long id,
            @RequestBody(required = false) CheckInReservationRequestDTO dto,
            @AuthenticationPrincipal UserDetails userDetails) {

        String username = extractUsername(userDetails);
        Reservation reservaAtualizada = (dto == null)
                ? reservationService.checkIn(id, username)
                : reservationService.checkIn(id, username, dto);
        return ResponseEntity.ok(ReservationResponseDTO.fromEntity(reservaAtualizada));
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<ReservationResponseDTO> cancelar(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {

        Reservation reservaAtualizada = reservationService.cancelReservation(id, extractUsername(userDetails));
        return ResponseEntity.ok(ReservationResponseDTO.fromEntity(reservaAtualizada));
    }

    public ResponseEntity<ReservationResponseDTO> finalizar(
            Long id,
            UserDetails userDetails) {
        return finalizar(id, null, userDetails);
    }

    @PatchMapping({"/{id}/finish", "/{id}/check-out"})
    public ResponseEntity<ReservationResponseDTO> finalizar(
            @PathVariable Long id,
            @RequestBody(required = false) FinishReservationRequestDTO dto,
            @AuthenticationPrincipal UserDetails userDetails) {

        String username = extractUsername(userDetails);
        Reservation reservaAtualizada = (dto == null)
                ? reservationService.finishReservation(id, username)
                : reservationService.finishReservation(id, username, dto);
        return ResponseEntity.ok(ReservationResponseDTO.fromEntity(reservaAtualizada));
    }

    public ResponseEntity<Void> excluirReserva(Long id) {
        return excluirReserva(id, null);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluirReserva(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {

        String email = extractUsername(userDetails);
        if (email != null) {
            reservationService.deleteReservation(id, email);
        } else {
            reservationService.deleteReservation(id);
        }
        return ResponseEntity.noContent().build();
    }

    private String extractUsername(UserDetails userDetails) {
        return (userDetails != null) ? userDetails.getUsername() : null;
    }
}
