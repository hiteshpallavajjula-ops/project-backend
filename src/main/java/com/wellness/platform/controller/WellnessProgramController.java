package com.wellness.platform.controller;

import com.wellness.platform.dto.MessageResponse;
import com.wellness.platform.model.*;
import com.wellness.platform.repository.*;
import com.wellness.platform.security.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/programs")
public class WellnessProgramController {

    @Autowired private WellnessProgramRepository programRepository;
    @Autowired private EnrollmentRepository enrollmentRepository;
    @Autowired private UserRepository userRepository;

    // ─── PUBLIC (no token required) ──────────────────────────────────────────

    @GetMapping
    public ResponseEntity<List<WellnessProgram>> getAllPrograms() {
        return ResponseEntity.ok(programRepository.findByActiveTrue());
    }

    @GetMapping("/{id}")
    public ResponseEntity<WellnessProgram> getProgramById(@PathVariable Long id) {
        return programRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ─── STUDENT ─────────────────────────────────────────────────────────────

    @PostMapping("/{id}/enroll")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<?> enrollInProgram(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));
        WellnessProgram program = programRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Program not found"));

        if (enrollmentRepository.existsByUserAndProgram(user, program)) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Already enrolled in this program."));
        }

        Enrollment enrollment = Enrollment.builder()
                .user(user)
                .program(program)
                .status(EnrollmentStatus.ACTIVE)
                .build();
        enrollmentRepository.save(enrollment);

        return ResponseEntity.ok(
                new MessageResponse("Successfully enrolled in " + program.getTitle()));
    }

    @GetMapping("/my-enrollments")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<List<Enrollment>> getMyEnrollments(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));
        return ResponseEntity.ok(enrollmentRepository.findByUser(user));
    }

    @DeleteMapping("/enrollments/{enrollmentId}")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<?> unenroll(
            @PathVariable Long enrollmentId,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        return enrollmentRepository.findById(enrollmentId).map(enrollment -> {
            if (!enrollment.getUser().getId().equals(userDetails.getId())) {
                return ResponseEntity.status(403).body(new MessageResponse("Unauthorized."));
            }
            enrollmentRepository.delete(enrollment);
            return ResponseEntity.ok(new MessageResponse("Successfully unenrolled."));
        }).orElse(ResponseEntity.notFound().build());
    }

    // ─── ADMIN ───────────────────────────────────────────────────────────────

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<WellnessProgram> createProgram(
            @RequestBody WellnessProgram program) {
        return ResponseEntity.ok(programRepository.save(program));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateProgram(
            @PathVariable Long id,
            @RequestBody WellnessProgram updated) {
        return programRepository.findById(id).map(p -> {
            p.setTitle(updated.getTitle());
            p.setDescription(updated.getDescription());
            p.setCategory(updated.getCategory());
            p.setDuration(updated.getDuration());
            p.setDifficulty(updated.getDifficulty());
            p.setPrice(updated.getPrice());
            p.setHighlights(updated.getHighlights());
            p.setActive(updated.isActive());
            return ResponseEntity.ok(programRepository.save(p));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteProgram(@PathVariable Long id) {
        programRepository.deleteById(id);
        return ResponseEntity.ok(new MessageResponse("Program deleted."));
    }

    @GetMapping("/all-enrollments")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Enrollment>> getAllEnrollments(
            @AuthenticationPrincipal UserDetailsImpl adminDetails) {
        List<Enrollment> allEnrollments = enrollmentRepository.findAll();
        List<Enrollment> filtered = allEnrollments.stream()
                .filter(e -> e.getUser() != null && adminDetails.getId().equals(e.getUser().getTrainerId()))
                .collect(java.util.stream.Collectors.toList());
        return ResponseEntity.ok(filtered);
    }

    /** Admin: confirm payment for an enrollment */
    @PatchMapping("/enrollments/{enrollmentId}/payment")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> confirmPayment(
            @PathVariable Long enrollmentId,
            @RequestBody java.util.Map<String, String> body,
            @AuthenticationPrincipal UserDetailsImpl adminDetails) {
        return enrollmentRepository.findById(enrollmentId).map(enrollment -> {
            if (enrollment.getUser() == null || !adminDetails.getId().equals(enrollment.getUser().getTrainerId())) {
                return ResponseEntity.status(403).body("Unauthorized to confirm payment for this student.");
            }
            enrollment.setPaymentStatus(body.getOrDefault("paymentStatus", "PAID"));
            return ResponseEntity.ok(enrollmentRepository.save(enrollment));
        }).orElse(ResponseEntity.notFound().build());
    }
}
