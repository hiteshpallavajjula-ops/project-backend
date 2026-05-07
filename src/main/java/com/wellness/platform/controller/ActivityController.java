package com.wellness.platform.controller;

import com.wellness.platform.dto.ActivityLogRequest;
import com.wellness.platform.model.*;
import com.wellness.platform.repository.*;
import com.wellness.platform.security.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/activity")
public class ActivityController {

    @Autowired private ActivityLogRepository activityLogRepository;
    @Autowired private UserRepository userRepository;

    /** Student logs their daily activity using a clean DTO (not the raw entity). */
    @PostMapping("/log")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ActivityLog> logActivity(
            @RequestBody ActivityLogRequest req,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Build the entity manually from the DTO
        ActivityLog log = new ActivityLog();
        log.setUser(user);
        log.setLogDate(req.getLogDate() != null ? req.getLogDate() : LocalDate.now());
        log.setStepCount(req.getStepCount());
        log.setWorkoutMinutes(req.getWorkoutMinutes());
        log.setWorkoutType(req.getWorkoutType());
        log.setWorkoutName(req.getWorkoutName());
        log.setWeightKg(req.getWeightKg());
        log.setHeightCm(req.getHeightCm());
        log.setWaterIntakeMl(req.getWaterIntakeMl());
        log.setCaloriesConsumed(req.getCaloriesConsumed());
        log.setNotes(req.getNotes());

        // Calculate BMI if weight and height are provided
        if (req.getWeightKg() != null && req.getHeightCm() != null && req.getHeightCm() > 0) {
            double hM = req.getHeightCm() / 100.0;
            log.setBmi(Math.round((req.getWeightKg() / (hM * hM)) * 10.0) / 10.0);
        }

        return ResponseEntity.ok(activityLogRepository.save(log));
    }

    /** Student views their own activity logs. */
    @GetMapping("/my-logs")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<List<ActivityLog>> getMyLogs(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));
        return ResponseEntity.ok(activityLogRepository.findByUserOrderByLogDateDesc(user));
    }

    /** Student gets their aggregate stats. */
    @GetMapping("/my-stats")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<Map<String, Object>> getMyStats(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Long totalSteps   = activityLogRepository.sumStepsByUser(user);
        Long totalMinutes = activityLogRepository.sumWorkoutMinutesByUser(user);
        List<ActivityLog> logs = activityLogRepository.findByUserOrderByLogDateDesc(user);

        double latestBmi = logs.stream()
                .filter(l -> l.getBmi() != null)
                .findFirst()
                .map(ActivityLog::getBmi)
                .orElse(0.0);

        return ResponseEntity.ok(Map.of(
                "totalSteps",          totalSteps   != null ? totalSteps   : 0L,
                "totalWorkoutMinutes", totalMinutes != null ? totalMinutes : 0L,
                "latestBmi",           latestBmi,
                "totalLogs",           (long) logs.size()
        ));
    }

    /** Admin: see all allotted students' activity. */
    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ActivityLog>> getAllActivity(
            @AuthenticationPrincipal UserDetailsImpl adminDetails) {
        List<ActivityLog> allLogs = activityLogRepository.findAllByOrderByLogDateDesc();
        List<ActivityLog> filtered = allLogs.stream()
                .filter(l -> l.getUser() != null && adminDetails.getId().equals(l.getUser().getTrainerId()))
                .collect(java.util.stream.Collectors.toList());
        return ResponseEntity.ok(filtered);
    }

    /** Admin: see a specific student's logs. */
    @GetMapping("/student/{studentId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ActivityLog>> getStudentActivity(
            @PathVariable Long studentId,
            @AuthenticationPrincipal UserDetailsImpl adminDetails) {
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));
        
        if (!adminDetails.getId().equals(student.getTrainerId())) {
            return ResponseEntity.status(403).build();
        }
        return ResponseEntity.ok(activityLogRepository.findByUserOrderByLogDateDesc(student));
    }
}
