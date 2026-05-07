package com.wellness.platform.controller;

import com.wellness.platform.dto.MessageResponse;
import com.wellness.platform.model.Role;
import com.wellness.platform.model.User;
import com.wellness.platform.repository.UserRepository;
import com.wellness.platform.security.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/trainers")
public class TrainerController {

    @Autowired
    private UserRepository userRepository;

    // Get all admins (Trainers)
    @GetMapping
    public ResponseEntity<List<User>> getAllTrainers() {
        // Return all users who have ROLE_ADMIN
        return ResponseEntity.ok(userRepository.findByRole(Role.ROLE_ADMIN));
    }

    // Student chooses an admin as their trainer
    @PostMapping("/{id}/choose")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<?> chooseTrainer(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        
        User student = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new RuntimeException("Student not found"));
        User trainer = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Trainer not found"));
        
        if (!trainer.getRole().equals(Role.ROLE_ADMIN)) {
            return ResponseEntity.badRequest().body(new MessageResponse("User is not an admin/trainer."));
        }
        
        student.setTrainerId(id);
        userRepository.save(student);
        
        return ResponseEntity.ok(new MessageResponse("Successfully selected " + trainer.getName() + " as your trainer."));
    }
    
    // Get the student's currently chosen trainer
    @GetMapping("/my")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<?> getMyTrainer(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        User student = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new RuntimeException("Student not found"));
                
        if (student.getTrainerId() == null) {
            return ResponseEntity.notFound().build(); // Or an empty OK response, but 404 is fine to say "no trainer"
        }
        
        return userRepository.findById(student.getTrainerId())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
