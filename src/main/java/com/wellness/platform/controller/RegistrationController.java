package com.wellness.platform.controller;

import com.wellness.platform.dto.MessageResponse;
import com.wellness.platform.model.Role;
import com.wellness.platform.model.User;
import com.wellness.platform.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Handles direct registration for both Students and Admins.
 * Email verification has been removed as per user request.
 * Users will configure MFA (Google Authenticator) upon login.
 */
@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
public class RegistrationController {

    @Autowired private UserRepository userRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    @PostMapping("/api/student/register")
    public ResponseEntity<?> studentRegister(@RequestBody Map<String, String> body) {
        return registerUser(body, Role.ROLE_STUDENT);
    }

    @PostMapping("/api/admin/register")
    public ResponseEntity<?> adminRegister(@RequestBody Map<String, String> body) {
        return registerUser(body, Role.ROLE_ADMIN);
    }

    private ResponseEntity<?> registerUser(Map<String, String> body, Role role) {
        String name     = body.get("name");
        String email    = body.get("email");
        String password = body.get("password");

        if (name == null || name.isBlank())     return bad("Name is required.");
        if (email == null || email.isBlank())   return bad("Email is required.");
        if (password == null || password.isBlank()) return bad("Password is required.");

        if (name.trim().equalsIgnoreCase(email.trim())) {
            return bad("Username and email cannot be the same.");
        }

        if (userRepository.existsByEmail(email)) {
            return bad("An account with this email already exists.");
        }
        if (userRepository.existsByName(name)) {
            return bad("An account with this username already exists.");
        }

        User user = User.builder()
                .name(name)
                .email(email)
                .password(passwordEncoder.encode(password))
                .role(role)
                .emailVerified(true) // Marking true since we removed email verification
                .mfaEnabled(false)   // MFA will be forced setup on first login
                .build();

        userRepository.save(user);

        return ResponseEntity.ok(new MessageResponse(
                "Account created successfully! You can now log in."));
    }

    private ResponseEntity<?> bad(String msg) {
        return ResponseEntity.badRequest().body(new MessageResponse(msg));
    }
}

