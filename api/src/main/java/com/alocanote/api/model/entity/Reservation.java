package com.alocanote.api.model.entity;

import com.alocanote.api.model.enums.ReservationStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Setter
@Getter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "reservations")
public class Reservation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime departureDateTime; // Data e Horário de Retirada

    @Column(nullable = false)
    private LocalDateTime returnDateTime; // Data e Horário de Devolução Prevista

    @Column(nullable = false)
    private String purpose; // Projeto / Finalidade no Promob

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReservationStatus status; // Alterado para o Enum correto

    // O relacionamento crucial que faltava com o equipamento
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "notebook_id", nullable = false)
    private Notebook notebook;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id", nullable = false)
    private Location location;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}