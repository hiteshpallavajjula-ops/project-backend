package com.wellness.platform.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "activity_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActivityLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private User user;

    private LocalDate logDate;

    private Integer stepCount;
    private Integer workoutMinutes;
    private String  workoutType;
    private String  workoutName;

    private Double weightKg;
    private Double heightCm;
    private Double bmi;

    private Integer waterIntakeMl;
    private Integer caloriesConsumed;

    private String notes;

    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
        if (logDate == null) logDate = LocalDate.now();
    }
}
