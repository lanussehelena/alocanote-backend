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

    List<Reservation> findByNotebookIdAndStatus(Long notebookId, ReservationStatus status);

    List<Reservation> findByNotebookId(Long notebookId);

    List<Reservation> findByUserId(Long userId);

    // Verifica se existe conflito de horário para o mesmo notebook com status ativos
    @Query("SELECT r FROM Reservation r WHERE r.notebook.id = :notebookId " +
            "AND r.status IN :statuses " +
            "AND (r.departureDateTime < :returnDateTime AND r.returnDateTime > :departureDateTime)")
    List<Reservation> findConflictingReservations(
            @Param("notebookId") Long notebookId,
            @Param("departureDateTime") LocalDateTime departureDateTime,
            @Param("returnDateTime") LocalDateTime returnDateTime,
            @Param("statuses") List<ReservationStatus> statuses
    );

    // Retorna todas as reservas ativas dentro de um intervalo de datas para o calendário
    @Query("SELECT r FROM Reservation r WHERE r.status IN :statuses " +
            "AND (r.departureDateTime <= :endOfPeriod AND r.returnDateTime >= :startOfPeriod) " +
            "ORDER BY r.departureDateTime ASC")
    List<Reservation> findActiveReservationsInPeriod(
            @Param("startOfPeriod") LocalDateTime startOfPeriod,
            @Param("endOfPeriod") LocalDateTime endOfPeriod,
            @Param("statuses") List<ReservationStatus> statuses
    );
}
