package com.example.wellnessbackend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "practitioner_profile")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PractitionerProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;

    @Column(nullable = false)
    private String specialization;

    @Column(nullable = false)
    private Boolean verified = false;

    @Column(nullable = false)
    private Double rating = 0.0;

    @Column(columnDefinition = "TEXT")
    private String bio;
}
