package com.alocanote.api.service;

import com.alocanote.api.dto.request.CreateReservationRequestDTO;
import com.alocanote.api.exception.BusinessException;
import com.alocanote.api.exception.ResourceNotFoundException;
import com.alocanote.api.model.entity.Location;
import com.alocanote.api.model.entity.Notebook;
import com.alocanote.api.model.entity.Reservation;
import com.alocanote.api.model.entity.User;
import com.alocanote.api.model.enums.NotebookStatus;
import com.alocanote.api.model.enums.ReservationStatus;
import com.alocanote.api.repository.LocationRepository;
import com.alocanote.api.repository.ReservationRepository;
import com.alocanote.api.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private NotebookService notebookService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private LocationRepository locationRepository;

    @InjectMocks
    private ReservationService reservationService;

    private User user;
    private Notebook notebook;
    private Location location;
    private LocalDateTime now;

    @BeforeEach
    void setUp() {
        now = LocalDateTime.now().plusDays(1);

        user = User.builder()
                .id(1L)
                .name("Maria Silva")
                .email("maria@alocanote.com")
                .build();

        notebook = Notebook.builder()
                .id(10L)
                .name("Dell Vostro 15")
                .modelDescription("Intel i7, 16GB RAM")
                .status(NotebookStatus.DISPONIVEL)
                .build();

        location = new Location();
        location.setId(100L);
        location.setName("Loja Central - João Pessoa");
    }

    @Test
    @DisplayName("Deve criar agendamento de notebook com sucesso")
    void shouldCreateReservationSuccessfully() {
        LocalDateTime start = now.plusHours(1);
        LocalDateTime end = now.plusHours(3);

        CreateReservationRequestDTO dto = new CreateReservationRequestDTO(
                notebook.getId(),
                start,
                end,
                location.getId(),
                "Visita externa a cliente"
        );

        when(notebookService.findById(notebook.getId())).thenReturn(notebook);
        when(reservationRepository.findConflictingReservations(eq(notebook.getId()), eq(start), eq(end), anyList()))
                .thenReturn(Collections.emptyList());
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(locationRepository.findById(location.getId())).thenReturn(Optional.of(location));
        when(reservationRepository.save(any(Reservation.class))).thenAnswer(invocation -> {
            Reservation saved = invocation.getArgument(0);
            saved.setId(1L);
            return saved;
        });

        Reservation result = reservationService.createReservation(dto, user.getEmail());

        assertNotNull(result);
        assertEquals(ReservationStatus.AGENDADO, result.getStatus());
        assertEquals(notebook, result.getNotebook());
        assertEquals(user, result.getUser());
        assertEquals(location, result.getLocation());
        verify(reservationRepository).save(any(Reservation.class));
    }

    @Test
    @DisplayName("Deve lançar BusinessException quando a data de término for anterior à data de início")
    void shouldThrowExceptionWhenEndTimeIsBeforeStartTime() {
        LocalDateTime start = now.plusHours(3);
        LocalDateTime end = now.plusHours(1);

        CreateReservationRequestDTO dto = new CreateReservationRequestDTO(
                notebook.getId(), start, end, location.getId(), "Propósito"
        );

        BusinessException ex = assertThrows(BusinessException.class, () ->
                reservationService.createReservation(dto, user.getEmail())
        );

        assertEquals("A data de término deve ser posterior à data de início.", ex.getMessage());
        verifyNoInteractions(notebookService, reservationRepository);
    }

    @Test
    @DisplayName("Deve lançar BusinessException quando a data de término for igual à data de início")
    void shouldThrowExceptionWhenEndTimeIsEqualStartTime() {
        LocalDateTime start = now.plusHours(1);

        CreateReservationRequestDTO dto = new CreateReservationRequestDTO(
                notebook.getId(), start, start, location.getId(), "Propósito"
        );

        BusinessException ex = assertThrows(BusinessException.class, () ->
                reservationService.createReservation(dto, user.getEmail())
        );

        assertEquals("A data de término deve ser posterior à data de início.", ex.getMessage());
        verifyNoInteractions(notebookService, reservationRepository);
    }

    @Test
    @DisplayName("Deve lançar BusinessException quando o notebook estiver em manutenção")
    void shouldThrowExceptionWhenNotebookIsInMaintenance() {
        notebook.setStatus(NotebookStatus.MANUTENCAO);
        LocalDateTime start = now.plusHours(1);
        LocalDateTime end = now.plusHours(2);

        CreateReservationRequestDTO dto = new CreateReservationRequestDTO(
                notebook.getId(), start, end, location.getId(), "Propósito"
        );

        when(notebookService.findById(notebook.getId())).thenReturn(notebook);

        BusinessException ex = assertThrows(BusinessException.class, () ->
                reservationService.createReservation(dto, user.getEmail())
        );

        assertEquals("Este notebook está em manutenção e não pode ser reservado.", ex.getMessage());
        verify(reservationRepository, never()).findConflictingReservations(any(), any(), any(), any());
    }

    @Test
    @DisplayName("Deve lançar BusinessException quando houver conflito com reserva AGENDADA")
    void shouldThrowExceptionWhenConflictWithAgendado() {
        LocalDateTime start = now.plusHours(1);
        LocalDateTime end = now.plusHours(3);

        CreateReservationRequestDTO dto = new CreateReservationRequestDTO(
                notebook.getId(), start, end, location.getId(), "Propósito"
        );

        Reservation conflito = Reservation.builder()
                .id(99L)
                .status(ReservationStatus.AGENDADO)
                .departureDateTime(start)
                .returnDateTime(end)
                .build();

        when(notebookService.findById(notebook.getId())).thenReturn(notebook);
        when(reservationRepository.findConflictingReservations(
                eq(notebook.getId()), eq(start), eq(end), eq(List.of(ReservationStatus.AGENDADO, ReservationStatus.CHECKED_IN))))
                .thenReturn(List.of(conflito));

        BusinessException ex = assertThrows(BusinessException.class, () ->
                reservationService.createReservation(dto, user.getEmail())
        );

        assertEquals("Este notebook já possui um agendamento conflitante para este intervalo de horário.", ex.getMessage());
        verify(reservationRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar BusinessException quando houver conflito com reserva CHECKED_IN")
    void shouldThrowExceptionWhenConflictWithCheckedIn() {
        LocalDateTime start = now.plusHours(1);
        LocalDateTime end = now.plusHours(3);

        CreateReservationRequestDTO dto = new CreateReservationRequestDTO(
                notebook.getId(), start, end, location.getId(), "Propósito"
        );

        Reservation conflito = Reservation.builder()
                .id(100L)
                .status(ReservationStatus.CHECKED_IN)
                .departureDateTime(start.minusHours(1))
                .returnDateTime(end.plusHours(1))
                .build();

        when(notebookService.findById(notebook.getId())).thenReturn(notebook);
        when(reservationRepository.findConflictingReservations(
                eq(notebook.getId()), eq(start), eq(end), eq(List.of(ReservationStatus.AGENDADO, ReservationStatus.CHECKED_IN))))
                .thenReturn(List.of(conflito));

        BusinessException ex = assertThrows(BusinessException.class, () ->
                reservationService.createReservation(dto, user.getEmail())
        );

        assertEquals("Este notebook já possui um agendamento conflitante para este intervalo de horário.", ex.getMessage());
        verify(reservationRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve realizar check-in com sucesso e alterar status do notebook para EM_USO")
    void shouldCheckInSuccessfully() {
        Reservation reserva = Reservation.builder()
                .id(1L)
                .status(ReservationStatus.AGENDADO)
                .notebook(notebook)
                .user(user)
                .build();

        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reserva));
        when(reservationRepository.save(any(Reservation.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Reservation result = reservationService.checkIn(1L, user.getEmail());

        assertEquals(ReservationStatus.CHECKED_IN, result.getStatus());
        assertNotNull(result.getCheckInAt());
        verify(notebookService).updateStatus(notebook.getId(), NotebookStatus.EM_USO);
        verify(reservationRepository).save(reserva);
    }

    @Test
    @DisplayName("Deve realizar check-in registrando local de retirada e coordenadas")
    void shouldCheckInWithLocationDetailsSuccessfully() {
        Reservation reserva = Reservation.builder()
                .id(1L)
                .status(ReservationStatus.AGENDADO)
                .notebook(notebook)
                .user(user)
                .build();

        com.alocanote.api.dto.request.CheckInReservationRequestDTO dto =
                new com.alocanote.api.dto.request.CheckInReservationRequestDTO(
                        "OUTRO_LOCAL",
                        "Av. Epitácio Pessoa, 1200 - João Pessoa, PB",
                        -7.1195,
                        -34.8450
                );

        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reserva));
        when(reservationRepository.save(any(Reservation.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Reservation result = reservationService.checkIn(1L, user.getEmail(), dto);

        assertEquals(ReservationStatus.CHECKED_IN, result.getStatus());
        assertEquals("OUTRO_LOCAL", result.getCheckInLocationType());
        assertEquals("Av. Epitácio Pessoa, 1200 - João Pessoa, PB", result.getCheckInAddress());
        assertEquals(-7.1195, result.getCheckInLatitude());
        assertEquals(-34.8450, result.getCheckInLongitude());
        assertNotNull(result.getCheckInAt());
    }

    @Test
    @DisplayName("Deve lançar BusinessException ao tentar check-in em reserva com status diferente de AGENDADO")
    void shouldThrowExceptionWhenCheckInInvalidStatus() {
        Reservation reserva = Reservation.builder()
                .id(1L)
                .status(ReservationStatus.CHECKED_IN)
                .notebook(notebook)
                .user(user)
                .build();

        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reserva));

        BusinessException ex = assertThrows(BusinessException.class, () ->
                reservationService.checkIn(1L, user.getEmail())
        );

        assertEquals("Só é possível fazer check-in de reservas com status AGENDADO.", ex.getMessage());
        verify(notebookService, never()).updateStatus(any(), any());
    }

    @Test
    @DisplayName("Deve cancelar reserva com sucesso e restaurar notebook para DISPONIVEL")
    void shouldCancelReservationSuccessfully() {
        Reservation reserva = Reservation.builder()
                .id(1L)
                .status(ReservationStatus.AGENDADO)
                .notebook(notebook)
                .user(user)
                .build();

        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reserva));
        when(reservationRepository.save(any(Reservation.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Reservation result = reservationService.cancelReservation(1L, user.getEmail());

        assertEquals(ReservationStatus.CANCELADO, result.getStatus());
        verify(notebookService).updateStatus(notebook.getId(), NotebookStatus.DISPONIVEL);
        verify(reservationRepository).save(reserva);
    }

    @Test
    @DisplayName("Deve lançar BusinessException ao tentar cancelar reserva já finalizada ou cancelada")
    void shouldThrowExceptionWhenCancelingFinishedReservation() {
        Reservation reserva = Reservation.builder()
                .id(1L)
                .status(ReservationStatus.FINALIZADO)
                .notebook(notebook)
                .user(user)
                .build();

        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reserva));

        BusinessException ex = assertThrows(BusinessException.class, () ->
                reservationService.cancelReservation(1L, user.getEmail())
        );

        assertEquals("Esta reserva já foi finalizada ou cancelada.", ex.getMessage());
        verify(notebookService, never()).updateStatus(any(), any());
    }

    @Test
    @DisplayName("Deve lançar BusinessException quando o usuário tentar alterar reserva de outro colaborador")
    void shouldThrowExceptionWhenUserDoesNotOwnReservation() {
        Reservation reserva = Reservation.builder()
                .id(1L)
                .status(ReservationStatus.AGENDADO)
                .notebook(notebook)
                .user(user)
                .build();

        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reserva));

        BusinessException ex = assertThrows(BusinessException.class, () ->
                reservationService.checkIn(1L, "outro@alocanote.com")
        );

        assertEquals("Acesso negado: esta reserva pertence a outro usuário.", ex.getMessage());
    }

    @Test
    @DisplayName("Deve finalizar reserva com sucesso e restaurar notebook para DISPONIVEL")
    void shouldFinishReservationSuccessfully() {
        Reservation reserva = Reservation.builder()
                .id(1L)
                .status(ReservationStatus.CHECKED_IN)
                .notebook(notebook)
                .user(user)
                .build();

        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reserva));
        when(reservationRepository.save(any(Reservation.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Reservation result = reservationService.finishReservation(1L, user.getEmail());

        assertEquals(ReservationStatus.FINALIZADO, result.getStatus());
        assertNotNull(result.getReturnedAt());
        verify(notebookService).updateStatus(notebook.getId(), NotebookStatus.DISPONIVEL);
        verify(reservationRepository).save(reserva);
    }

    @Test
    @DisplayName("Deve finalizar reserva registrando local de devolução e foto automática")
    void shouldFinishReservationWithLocationAndPhotoSuccessfully() {
        Reservation reserva = Reservation.builder()
                .id(1L)
                .status(ReservationStatus.CHECKED_IN)
                .notebook(notebook)
                .user(user)
                .build();

        com.alocanote.api.dto.request.FinishReservationRequestDTO dto =
                new com.alocanote.api.dto.request.FinishReservationRequestDTO(
                        "LOJA",
                        "Loja Central - João Pessoa, PB",
                        -7.1153,
                        -34.8610,
                        "data:image/jpeg;base64,/9j/4AAQSkZJRg=="
                );

        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reserva));
        when(reservationRepository.save(any(Reservation.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Reservation result = reservationService.finishReservation(1L, user.getEmail(), dto);

        assertEquals(ReservationStatus.FINALIZADO, result.getStatus());
        assertEquals("LOJA", result.getReturnLocationType());
        assertEquals("Loja Central - João Pessoa, PB", result.getReturnAddress());
        assertEquals(-7.1153, result.getReturnLatitude());
        assertEquals(-34.8610, result.getReturnLongitude());
        assertEquals("data:image/jpeg;base64,/9j/4AAQSkZJRg==", result.getReturnPhotoUrl());
        assertNotNull(result.getReturnedAt());
    }

    @Test
    @DisplayName("Deve lançar BusinessException ao tentar finalizar reserva que não esteja CHECKED_IN")
    void shouldThrowExceptionWhenFinishingReservationNotCheckedIn() {
        Reservation reserva = Reservation.builder()
                .id(1L)
                .status(ReservationStatus.AGENDADO)
                .notebook(notebook)
                .user(user)
                .build();

        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reserva));

        BusinessException ex = assertThrows(BusinessException.class, () ->
                reservationService.finishReservation(1L, user.getEmail())
        );

        assertEquals("Só é possível finalizar reservas que estejam em andamento (CHECKED_IN).", ex.getMessage());
        verify(notebookService, never()).updateStatus(any(), any());
    }

    @Test
    @DisplayName("Deve retornar reservas ativas do período para o calendário")
    void shouldFindCalendarReservationsSuccessfully() {
        Reservation reserva = Reservation.builder()
                .id(1L)
                .status(ReservationStatus.AGENDADO)
                .departureDateTime(LocalDateTime.of(2026, 11, 15, 9, 0))
                .returnDateTime(LocalDateTime.of(2026, 11, 15, 12, 0))
                .notebook(notebook)
                .user(user)
                .location(location)
                .build();

        when(reservationRepository.findActiveReservationsInPeriod(
                eq(LocalDateTime.of(2026, 11, 1, 0, 0)),
                eq(LocalDateTime.of(2026, 11, 30, 23, 59, 59)),
                eq(List.of(ReservationStatus.AGENDADO, ReservationStatus.CHECKED_IN))
        )).thenReturn(List.of(reserva));

        List<Reservation> result = reservationService.findCalendarReservations(11, 2026);

        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getId());
    }

    @Test
    @DisplayName("Deve lançar BusinessException quando o mês informado para o calendário for inválido")
    void shouldThrowExceptionWhenCalendarMonthIsInvalid() {
        BusinessException ex = assertThrows(BusinessException.class, () ->
                reservationService.findCalendarReservations(13, 2026)
        );

        assertEquals("O mês informado é inválido. Informe um valor entre 1 e 12.", ex.getMessage());
    }
}
