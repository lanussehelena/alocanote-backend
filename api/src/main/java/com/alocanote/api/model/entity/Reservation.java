package com.alocanote.api.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
@Entity
@Table(name = "reservations")
public class Reservation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime departureDateTime; // Data e Horário de Saída

    @Column(nullable = false)
    private LocalDateTime returnDateTime; // Data e Horário de Devolução

    @Column(nullable = false)
    private String purpose; // Projeto / Finalidade

    @Column(nullable = false)
    private String status; // Ex: Agendado, Concluído, Cancelado

    // Relacionamento ManyToOne: Muitas reservas pertencem a um único Local de Retirada
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id", nullable = false)
    private Location location;

    // Relacionamento ManyToOne: Muitas reservas podem ser feitas por um usuário
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user; // Supondo que você possua uma entidade User configurada

    // Construtores, Getters e Setters
    public Reservation() {}

}

