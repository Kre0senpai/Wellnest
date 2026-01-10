package com.example.wellnessbackend.dto;

import lombok.Data;

@Data
public class TherapyDto {
    private Long practitionerId;
    private String name;
    private String description;
    private Double price;
    private Integer duration;
    private String category;
    private String imageUrl;
}
