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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final NotebookService notebookService;
    private final UserRepository userRepository;
    private final LocationRepository locationRepository;

    public ReservationService(ReservationRepository reservationRepository,
                              NotebookService notebookService,
                              UserRepository userRepository,
                              LocationRepository locationRepository) {
        this.reservationRepository = reservationRepository;
        this.notebookService = notebookService;
        this.userRepository = userRepository;
        this.locationRepository = locationRepository;
    }

    @Transactional
    public Reservation createReservation(CreateReservationRequestDTO dto, String userEmail) {
        if (!dto.endTime().isAfter(dto.startTime())) {
            throw new BusinessException("A data de término deve ser posterior à data de início.");
        }

        Notebook notebook = notebookService.findById(dto.notebookId());

        if (notebook.getStatus() == NotebookStatus.MANUTENCAO) {
            throw new BusinessException("Este notebook está em manutenção e não pode ser reservado.");
        }

        List<ReservationStatus> activeStatuses = List.of(ReservationStatus.AGENDADO, ReservationStatus.CHECKED_IN);

        List<Reservation> conflitos = reservationRepository.findConflictingReservations(
                dto.notebookId(),
                dto.startTime(),
                dto.endTime(),
                activeStatuses
        );

        if (!conflitos.isEmpty()) {
            throw new BusinessException("Este notebook já possui um agendamento conflitante para este intervalo de horário.");
        }

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário autenticado não encontrado."));

        Location location = locationRepository.findById(dto.locationId())
                .orElseThrow(() -> new ResourceNotFoundException("Local de retirada não encontrado com o ID: " + dto.locationId()));

        Reservation reservation = Reservation.builder()
                .notebook(notebook)
                .user(user)
                .location(location)
                .departureDateTime(dto.startTime())
                .returnDateTime(dto.endTime())
                .purpose(dto.purpose() != null && !dto.purpose().isBlank() ? dto.purpose() : "Uso padrão")
                .status(ReservationStatus.AGENDADO)
                .build();

        return reservationRepository.save(reservation);
    }

    // Retorna as reservas pertencentes ao usuário autenticado
    @Transactional(readOnly = true)
    public List<Reservation> findMyReservations(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado."));
        return reservationRepository.findByUserId(user.getId());
    }

    // Busca reserva por ID garantindo a posse do recurso
    @Transactional(readOnly = true)
    public Reservation findById(Long reservationId, String userEmail) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException("Reserva não encontrada com o ID: " + reservationId));

        validarPosseReserva(reservation, userEmail);
        return reservation;
    }

    // Realiza o Check-in (atualiza status para CHECKED_IN)
    @Transactional
    public Reservation checkIn(Long reservationId, String userEmail) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException("Reserva não encontrada com o ID: " + reservationId));

        validarPosseReserva(reservation, userEmail);

        if (reservation.getStatus() != ReservationStatus.AGENDADO) {
            throw new BusinessException("Só é possível fazer check-in de reservas com status AGENDADO.");
        }

        reservation.setStatus(ReservationStatus.CHECKED_IN);
        notebookService.updateStatus(reservation.getNotebook().getId(), NotebookStatus.EM_USO);

        return reservationRepository.save(reservation);
    }

    // Realiza o cancelamento da reserva e libera o equipamento
    @Transactional
    public Reservation cancelReservation(Long reservationId, String userEmail) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException("Reserva não encontrada com o ID: " + reservationId));

        validarPosseReserva(reservation, userEmail);

        if (reservation.getStatus() == ReservationStatus.FINALIZADO || reservation.getStatus() == ReservationStatus.CANCELADO) {
            throw new BusinessException("Esta reserva já foi finalizada ou cancelada.");
        }

        reservation.setStatus(ReservationStatus.CANCELADO);
        notebookService.updateStatus(reservation.getNotebook().getId(), NotebookStatus.DISPONIVEL);

        return reservationRepository.save(reservation);
    }

    // Realiza a finalização/devolução da reserva e libera o equipamento
    @Transactional
    public Reservation finishReservation(Long reservationId, String userEmail) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException("Reserva não encontrada com o ID: " + reservationId));

        validarPosseReserva(reservation, userEmail);

        if (reservation.getStatus() != ReservationStatus.CHECKED_IN) {
            throw new BusinessException("Só é possível finalizar reservas que estejam em andamento (CHECKED_IN).");
        }

        reservation.setStatus(ReservationStatus.FINALIZADO);
        notebookService.updateStatus(reservation.getNotebook().getId(), NotebookStatus.DISPONIVEL);

        return reservationRepository.save(reservation);
    }

    private void validarPosseReserva(Reservation reservation, String userEmail) {
        if (!reservation.getUser().getEmail().equals(userEmail)) {
            throw new BusinessException("Acesso negado: esta reserva pertence a outro usuário.");
        }
    }
}