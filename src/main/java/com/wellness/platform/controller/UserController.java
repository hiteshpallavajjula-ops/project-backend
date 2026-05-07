package com.wellness.platform.controller;

import com.wellness.platform.dto.MessageResponse;
import com.wellness.platform.model.User;
import com.wellness.platform.repository.ActivityLogRepository;
import com.wellness.platform.repository.AdminSuggestionRepository;
import com.wellness.platform.repository.EnrollmentRepository;
import com.wellness.platform.repository.UserRepository;
import com.wellness.platform.security.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import jakarta.transaction.Transactional;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired private UserRepository userRepository;
    @Autowired private EnrollmentRepository enrollmentRepository;
    @Autowired private ActivityLogRepository activityLogRepository;
    @Autowired private AdminSuggestionRepository adminSuggestionRepository;

    @GetMapping("/me")
    public ResponseEntity<?> getMyProfile(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));
        return ResponseEntity.ok(user);
    }

    @DeleteMapping("/me")
    @Transactional
    public ResponseEntity<?> deleteMyAccount(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Delete enrollments
        enrollmentRepository.deleteAll(enrollmentRepository.findByUser(user));

        // Delete activity logs (correct method name)
        activityLogRepository.deleteAll(activityLogRepository.findByUserOrderByLogDateDesc(user));

        // Delete suggestions where user is the student
        adminSuggestionRepository.deleteAll(adminSuggestionRepository.findByStudentOrderByCreatedAtDesc(user));

        // Delete suggestions where user is the admin/trainer
        adminSuggestionRepository.deleteAll(adminSuggestionRepository.findByAdminOrderByCreatedAtDesc(user));

        // Finally delete the user
        userRepository.delete(user);

        return ResponseEntity.ok(new MessageResponse("Account deleted permanently."));
    }

    /** Update profile details (email, phone) */
    @PutMapping("/me")
    public ResponseEntity<?> updateMyProfile(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestBody java.util.Map<String, String> body) {
        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        String newEmail = body.get("email");
        String newPhone = body.get("phone");

        // Validate and update email
        if (newEmail != null && !newEmail.isBlank() && !newEmail.equals(user.getEmail())) {
            if (userRepository.existsByEmail(newEmail)) {
                return ResponseEntity.badRequest().body(new MessageResponse("This email is already in use by another account."));
            }
            user.setEmail(newEmail.trim());
        }

        // Update phone
        if (newPhone != null) {
            user.setPhone(newPhone.isBlank() ? null : newPhone.trim());
        }

        userRepository.save(user);
        return ResponseEntity.ok(user);
    }


    @PostMapping("/me/photo")
    public ResponseEntity<?> uploadProfilePhoto(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestBody java.util.Map<String, String> payload) {
        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        String base64Photo = payload.get("photo");
        if (base64Photo == null) {
            return ResponseEntity.badRequest().body(new MessageResponse("Photo data is required"));
        }
        
        user.setProfilePhoto(base64Photo);
        userRepository.save(user);
        
        return ResponseEntity.ok(new MessageResponse("Profile photo updated successfully"));
    }
}
