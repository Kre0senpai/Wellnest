package com.example.wellnessbackend.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PractitionerResponseDto {
    private Long id;
    private Long userId;
    private String specialization;
    private Boolean verified;
    private Double rating;
    private String bio;
}
