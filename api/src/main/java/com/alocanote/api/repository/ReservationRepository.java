package com.alocanote.api.repository;

import com.alocanote.api.model.entity.Reservation;
import com.alocanote.api.model.enums.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    // verifica se o computador já está ocupado
    List<Reservation> findByNotebookIdAndStatus(Long notebookId, ReservationStatus status);

    // lista o histórico de reservas de um colaborador
    List<Reservation> findByUserId(Long userId);

    // NOVO: Verifica se existe conflito de horário para o mesmo notebook
    @Query("SELECT r FROM Reservation r WHERE r.notebook.id = :notebookId " +
            "AND r.status = :status " +
            "AND ((r.departureDateTime <= :returnDateTime) AND (r.returnDateTime >= :departureDateTime))")
    List<Reservation> findConflictingReservations(
            @Param("notebookId") Long notebookId,
            @Param("departureDateTime") LocalDateTime departureDateTime,
            @Param("returnDateTime") LocalDateTime returnDateTime,
            @Param("status") ReservationStatus status
    );
}
