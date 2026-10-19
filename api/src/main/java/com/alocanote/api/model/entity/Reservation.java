package com.alocanote.api.model.entity;

import com.alocanote.api.model.enums.ReservationStatus;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "reservations")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime departureDateTime;

    @Column(nullable = false)
    private LocalDateTime returnDateTime;

    @Column(nullable = false)
    private String purpose;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReservationStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "notebook_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "reservations"})
    private Notebook notebook;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "reservations"})
    private Location location;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "reservations", "password"})
    private User user;

    @Column(name = "check_in_location_type")
    private String checkInLocationType;

    @Column(name = "check_in_address", length = 1000)
    private String checkInAddress;

    @Column(name = "check_in_latitude")
    private Double checkInLatitude;

    @Column(name = "check_in_longitude")
    private Double checkInLongitude;

    @Column(name = "check_in_at")
    private LocalDateTime checkInAt;

    @Column(name = "return_location_type")
    private String returnLocationType;

    @Column(name = "return_address", length = 1000)
    private String returnAddress;

    @Column(name = "return_latitude")
    private Double returnLatitude;

    @Column(name = "return_longitude")
    private Double returnLongitude;

    @Column(name = "return_photo_url", length = 10485760)
    private String returnPhotoUrl;

    @Column(name = "returned_at")
    private LocalDateTime returnedAt;
}