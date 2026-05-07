package com.wellness.platform.controller;

import com.wellness.platform.model.*;
import com.wellness.platform.repository.*;
import com.wellness.platform.security.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/suggestions")
public class SuggestionController {

    @Autowired private AdminSuggestionRepository suggestionRepository;
    @Autowired private UserRepository userRepository;

    /** Admin: get list of all students (for the dropdown selector). */
    @GetMapping("/students")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Map<String, Object>>> getAllStudents(
            @AuthenticationPrincipal UserDetailsImpl adminDetails) {
        List<Map<String, Object>> students = userRepository.findAll().stream()
                .filter(u -> u.getRole() == Role.ROLE_STUDENT && adminDetails.getId().equals(u.getTrainerId()))
                .map(u -> {
                    Map<String, Object> map = new java.util.HashMap<>();
                    map.put("id", u.getId());
                    map.put("name", u.getName());
                    map.put("email", u.getEmail());
                    map.put("trainerId", u.getTrainerId());
                    map.put("profilePhoto", u.getProfilePhoto());
                    return map;
                })
                .collect(Collectors.toList());
        return ResponseEntity.ok(students);
    }

    /** Admin: send a suggestion to a student. */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AdminSuggestion> createSuggestion(
            @RequestBody Map<String, Object> body,
            @AuthenticationPrincipal UserDetailsImpl adminDetails) {

        User admin = userRepository.findById(adminDetails.getId())
                .orElseThrow(() -> new RuntimeException("Admin not found"));
        User student = userRepository.findById(
                Long.valueOf(body.get("studentId").toString()))
                .orElseThrow(() -> new RuntimeException("Student not found"));

        if (!admin.getId().equals(student.getTrainerId())) {
            return ResponseEntity.badRequest().build();
        }

        AdminSuggestion suggestion = AdminSuggestion.builder()
                .admin(admin)
                .student(student)
                .message(body.get("message").toString())
                .type(SuggestionType.valueOf(body.get("type").toString()))
                .read(false)
                .build();

        return ResponseEntity.ok(suggestionRepository.save(suggestion));
    }

    /** Admin: view all suggestions sent. */
    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AdminSuggestion>> getAllSuggestions(
            @AuthenticationPrincipal UserDetailsImpl adminDetails) {
        User admin = userRepository.findById(adminDetails.getId())
                .orElseThrow(() -> new RuntimeException("Admin not found"));
        return ResponseEntity.ok(
                suggestionRepository.findByAdminOrderByCreatedAtDesc(admin));
    }

    /** Student: view their own suggestions. */
    @GetMapping("/my")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<List<AdminSuggestion>> getMySuggestions(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        User student = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));
        return ResponseEntity.ok(
                suggestionRepository.findByStudentOrderByCreatedAtDesc(student));
    }

    /** Student: mark a suggestion as read. */
    @PutMapping("/{id}/read")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<?> markAsRead(@PathVariable Long id) {
        return suggestionRepository.findById(id).map(s -> {
            s.setRead(true);
            return ResponseEntity.ok(suggestionRepository.save(s));
        }).orElse(ResponseEntity.notFound().build());
    }
}
