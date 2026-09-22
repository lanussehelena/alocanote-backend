package com.alocanote.api.model;

import com.alocanote.api.model.enums.Role;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "tb_user")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false, unique = true)
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(name = "role_enum", nullable = false)
    private Role role;

    @Column(name = "custom_role")
    private String customRole;

    @Column(name = "access_level", nullable = false)
    private String accessLevel; // ROLE_ADMIN ou ROLE_USER

    private String profilePictureUrl;

    private String street;
    private String number;
    private String neighborhood;
    private String city;
    private String state;
    private String zipCode;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.accessLevel == null) {
            this.accessLevel = (this.role == Role.ADMINISTRADOR) ? "ROLE_ADMIN" : "ROLE_USER";
        }
    }
}
