package com.alocanote.api.service;

import com.alocanote.api.model.entity.Notebook;
import com.alocanote.api.model.entity.Reservation;
import com.alocanote.api.model.entity.User;
import com.alocanote.api.model.enums.NotebookStatus;
import com.alocanote.api.model.enums.ReservationStatus;
import com.alocanote.api.repository.ReservationRepository;
import com.alocanote.api.repository.UserRepository;
import com.alocanote.api.exception.BusinessException;
import com.alocanote.api.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final NotebookService notebookService;
    private final UserRepository userRepository;

    public ReservationService(ReservationRepository reservationRepository,
                              NotebookService notebookService,
                              UserRepository userRepository) {
        this.reservationRepository = reservationRepository;
        this.notebookService = notebookService;
        this.userRepository = userRepository;
    }

    @Transactional
    public Reservation createReservation(Long notebookId, Long userId) {
        Notebook notebook = notebookService.findById(notebookId);

        // Regra 1: O equipamento tem de estar livre no momento da solicitação
        if (notebook.getStatus() != NotebookStatus.DISPONIVEL) {
            throw new BusinessException("Este notebook já se encontra em uso ou em manutenção.");
        }

        // Regra 2: Bloqueio duplo para garantir que não há um agendamento prévio na base de dados
        List<Reservation> activeReservations = reservationRepository.findByNotebookIdAndStatus(notebookId, ReservationStatus.AGENDADO);
        if (!activeReservations.isEmpty()) {
            throw new BusinessException("Conflito: Este notebook já foi reservado por outra colaboradora.");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Colaboradora não encontrada."));

        Reservation reservation = Reservation.builder()
                .notebook(notebook)
                .user(user)
                .status(ReservationStatus.AGENDADO)
                .departureDateTime(LocalDateTime.now())
                .returnDateTime(LocalDateTime.now().plusHours(4))
                .purpose("Uso padrão")
                .build();

        // Bloqueia o notebook para as outras colaboradoras instantaneamente
        notebookService.updateStatus(notebookId, NotebookStatus.EM_USO);

        return reservationRepository.save(reservation);
    }
}