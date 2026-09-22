package com.alocanote.api.model;

import com.alocanote.api.model.enums.NotebookStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "tb_notebook")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notebook {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(name = "model_description", nullable = false)
    private String modelDescription;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotebookStatus status;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.status == null) {
            this.status = NotebookStatus.DISPONIVEL;
        }
    }
}
