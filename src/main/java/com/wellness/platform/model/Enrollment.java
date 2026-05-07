package com.wellness.platform.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "enrollments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Enrollment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Only serialize id, name, email — not the full user graph
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "enrollments", "activities"})
    private User user;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "program_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private WellnessProgram program;

    private LocalDateTime enrolledAt;

    @Enumerated(EnumType.STRING)
    private EnrollmentStatus status;

    private String paymentStatus; // "PENDING", "PAID", "FREE"

    @PrePersist
    public void prePersist() {
        enrolledAt = LocalDateTime.now();
        if (status == null) status = EnrollmentStatus.ACTIVE;
        if (paymentStatus == null) {
            if (program != null && program.getPrice() != null && program.getPrice() > 0) {
                paymentStatus = "PENDING";
            } else {
                paymentStatus = "FREE";
            }
        }
    }
}
