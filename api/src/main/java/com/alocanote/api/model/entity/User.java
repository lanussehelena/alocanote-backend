package com.alocanote.api.model.entity;

import com.alocanote.api.model.enums.Role;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "tb_user")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
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

    @JsonIgnore
    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(name = "role_enum", nullable = false)
    private Role role;

    @Column(name = "custom_role")
    private String customRole;

    @Column(name = "access_level", nullable = false)
    private String accessLevel;

    @Column(name = "profile_picture_url", length = 10485760)
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
