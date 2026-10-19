package com.alocanote.api.controller;

import com.alocanote.api.dto.request.CreateReservationRequestDTO;
import com.alocanote.api.dto.response.ReservationResponseDTO;
import com.alocanote.api.model.entity.Reservation;
import com.alocanote.api.model.enums.ReservationStatus;
import com.alocanote.api.service.ReservationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservationControllerTest {

    @Mock
    private ReservationService reservationService;

    @InjectMocks
    private ReservationController reservationController;

    private final UserDetails userDetails = new User("maria@alocanote.com", "password", Collections.emptyList());

    @Test
    @DisplayName("Deve agendar notebook retornando ReservationResponseDTO com HTTP 201")
    void shouldCreateReservationSuccessfully() {
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = start.plusHours(2);
        CreateReservationRequestDTO request = new CreateReservationRequestDTO(1L, start, end, 10L, "Apresentação");

        Reservation reservation = Reservation.builder()
                .id(100L)
                .departureDateTime(start)
                .returnDateTime(end)
                .purpose("Apresentação")
                .status(ReservationStatus.AGENDADO)
                .build();

        when(reservationService.createReservation(request, userDetails.getUsername())).thenReturn(reservation);

        ResponseEntity<ReservationResponseDTO> response = reservationController.agendarNotebook(request, userDetails);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(100L, response.getBody().id());
        assertEquals(ReservationStatus.AGENDADO, response.getBody().status());
    }

    @Test
    @DisplayName("Deve listar minhas reservas retornando lista de ReservationResponseDTO")
    void shouldListMyReservations() {
        Reservation reservation = Reservation.builder()
                .id(100L)
                .departureDateTime(LocalDateTime.now().plusDays(1))
                .returnDateTime(LocalDateTime.now().plusDays(1).plusHours(2))
                .status(ReservationStatus.AGENDADO)
                .build();

        when(reservationService.findMyReservations(userDetails.getUsername())).thenReturn(List.of(reservation));

        ResponseEntity<List<ReservationResponseDTO>> response = reservationController.listarMinhasReservas(userDetails);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals(100L, response.getBody().get(0).id());
    }

    @Test
    @DisplayName("Deve realizar check-in retornando ReservationResponseDTO")
    void shouldCheckInSuccessfully() {
        Reservation reservation = Reservation.builder()
                .id(100L)
                .status(ReservationStatus.CHECKED_IN)
                .build();

        when(reservationService.checkIn(100L, userDetails.getUsername())).thenReturn(reservation);

        ResponseEntity<ReservationResponseDTO> response = reservationController.checkIn(100L, userDetails);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(ReservationStatus.CHECKED_IN, response.getBody().status());
    }

    @Test
    @DisplayName("Deve cancelar reserva retornando ReservationResponseDTO")
    void shouldCancelReservationSuccessfully() {
        Reservation reservation = Reservation.builder()
                .id(100L)
                .status(ReservationStatus.CANCELADO)
                .build();

        when(reservationService.cancelReservation(100L, userDetails.getUsername())).thenReturn(reservation);

        ResponseEntity<ReservationResponseDTO> response = reservationController.cancelar(100L, userDetails);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(ReservationStatus.CANCELADO, response.getBody().status());
    }

    @Test
    @DisplayName("Deve finalizar/devolver reserva retornando ReservationResponseDTO")
    void shouldFinishReservationSuccessfully() {
        Reservation reservation = Reservation.builder()
                .id(100L)
                .status(ReservationStatus.FINALIZADO)
                .build();

        when(reservationService.finishReservation(100L, userDetails.getUsername())).thenReturn(reservation);

        ResponseEntity<ReservationResponseDTO> response = reservationController.finalizar(100L, userDetails);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(ReservationStatus.FINALIZADO, response.getBody().status());
    }

    @Test
    @DisplayName("Deve listar reservas do calendário por mês e ano")
    void shouldListCalendarReservations() {
        Reservation reservation = Reservation.builder()
                .id(200L)
                .departureDateTime(LocalDateTime.of(2026, 11, 15, 9, 0))
                .returnDateTime(LocalDateTime.of(2026, 11, 15, 12, 0))
                .status(ReservationStatus.AGENDADO)
                .build();

        when(reservationService.findCalendarReservations(11, 2026)).thenReturn(List.of(reservation));

        ResponseEntity<List<ReservationResponseDTO>> response = reservationController.listarCalendario(11, 2026);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals(200L, response.getBody().get(0).id());
        verify(reservationService).findCalendarReservations(11, 2026);
    }
}
