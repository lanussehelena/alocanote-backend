package com.alocanote.api.repository;

import com.alocanote.api.model.entity.Reservation;
import com.alocanote.api.model.enums.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    // verifica se o computador já está ocupado
    List<Reservation> findByNotebookIdAndStatus(Long notebookId, ReservationStatus status);

    // lista o histórico de reservas de um colaborador
    List<Reservation> findByUserId(Long userId);
}