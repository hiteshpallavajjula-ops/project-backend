package com.wellness.platform.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "wellness_programs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WellnessProgram {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    private ProgramCategory category; // WORKOUT, DIET, MENTAL_HEALTH, FITNESS

    private String imageUrl;
    private String duration; // e.g. "4 Weeks"
    private String difficulty; // Beginner, Intermediate, Advanced
    private Double price;

    @Column(columnDefinition = "TEXT")
    private String highlights; // comma-separated bullet points

    private boolean active = true;
}
