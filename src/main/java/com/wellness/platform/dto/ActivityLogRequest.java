package com.wellness.platform.dto;

import lombok.Data;
import java.time.LocalDate;

/**
 * DTO for receiving activity log data from the frontend.
 * Avoids binding a raw JPA entity from the HTTP request body.
 */
@Data
public class ActivityLogRequest {
    private LocalDate logDate;
    private Integer stepCount;
    private Integer workoutMinutes;
    private String  workoutType;
    private String  workoutName;
    private Double  weightKg;
    private Double  heightCm;
    private Integer waterIntakeMl;
    private Integer caloriesConsumed;
    private String  notes;
}
