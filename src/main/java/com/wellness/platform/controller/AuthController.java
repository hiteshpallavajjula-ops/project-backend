package com.wellness.platform.controller;

import com.wellness.platform.dto.*;
import com.wellness.platform.model.Role;
import com.wellness.platform.model.User;
import com.wellness.platform.repository.UserRepository;
import com.wellness.platform.security.JwtUtils;
import com.wellness.platform.security.UserDetailsImpl;
import com.wellness.platform.service.MfaService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    UserRepository userRepository;

    @Autowired
    PasswordEncoder encoder;

    @Autowired
    JwtUtils jwtUtils;

    @Autowired
    MfaService mfaService;

    @Autowired
    com.wellness.platform.service.PhoneOtpService phoneOtpService;

    /** Send a 6-digit OTP to the given phone number for verification */
    @PostMapping("/send-otp")
    public ResponseEntity<?> sendOtp(@RequestBody java.util.Map<String, String> body) {
        String phone = body.get("phone");
        if (phone == null || phone.isBlank())
            return ResponseEntity.badRequest().body(new MessageResponse("Phone number is required."));
        try {
            phoneOtpService.sendOtp(phone);
            return ResponseEntity.ok(new MessageResponse("OTP sent to " + phone));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new MessageResponse("Failed to send OTP: " + e.getMessage()));
        }
    }

    /** Verify the OTP for the given phone number */
    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(@RequestBody java.util.Map<String, String> body) {
        String phone = body.get("phone");
        String code  = body.get("code");
        boolean valid = phoneOtpService.verifyOtp(phone, code);
        if (valid) return ResponseEntity.ok(new MessageResponse("Phone number verified successfully."));
        return ResponseEntity.badRequest().body(new MessageResponse("Invalid or expired OTP."));
    }

    /** Combined Verify OTP and Register user in one step */
    @PostMapping("/verify-register")
    public ResponseEntity<?> verifyAndRegister(@RequestBody java.util.Map<String, String> body) {
        String email = body.get("email");
        String phone = body.get("phone");
        String code = body.get("otp");
        
        if (userRepository.existsByEmail(email)) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Email is already in use!"));
        }

        boolean valid = phoneOtpService.verifyOtp(phone, code);
        if (!valid) {
            return ResponseEntity.badRequest().body(new MessageResponse("Invalid or expired OTP."));
        }

        Role role = Role.ROLE_STUDENT;
        if (body.get("role") != null && body.get("role").equalsIgnoreCase("admin")) {
            role = Role.ROLE_ADMIN;
        }

        // Create new user's account
        User user = User.builder()
                .name(body.get("name"))
                .email(email)
                .phone(phone)
                .password(encoder.encode(body.get("password")))
                .role(role)
                .emailVerified(false)
                .phoneVerified(true)
                .mfaEnabled(false)
                .build();

        userRepository.save(user);

        // Generate JWT token for immediate login
        UserDetailsImpl userDetails = UserDetailsImpl.build(user);
        String jwt = jwtUtils.generateJwtToken(userDetails);

        return ResponseEntity.ok(new JwtResponse(jwt, user.getId(), user.getName(), 
                user.getEmail(), user.getRole().name(), false));
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterRequest signUpRequest) {
        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Email is already in use!"));
        }

        Role role = Role.ROLE_STUDENT;
        if (signUpRequest.getRole() != null && signUpRequest.getRole().equalsIgnoreCase("admin")) {
            role = Role.ROLE_ADMIN;
        }

        // Create new user's account
        User user = User.builder()
                .name(signUpRequest.getName())
                .email(signUpRequest.getEmail())
                .password(encoder.encode(signUpRequest.getPassword()))
                .role(role)
                .emailVerified(false)
                .mfaEnabled(false)
                .build();

        userRepository.save(user);

        return ResponseEntity.ok(new MessageResponse("User registered successfully! Proceed to setup MFA or verify email."));
    }

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getName(), loginRequest.getPassword()));

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        // If MFA is enabled, do not return JWT yet. Return a flag indicating MFA is required.
        if (userDetails.isMfaEnabled()) {
            return ResponseEntity.ok(new JwtResponse(null, userDetails.getId(), userDetails.getName(), 
                    userDetails.getEmail(), userDetails.getAuthorities().iterator().next().getAuthority(), true));
        }

        // If MFA not enabled, generate token normally
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(userDetails);

        return ResponseEntity.ok(new JwtResponse(jwt, userDetails.getId(), userDetails.getName(), 
                userDetails.getEmail(), userDetails.getAuthorities().iterator().next().getAuthority(), false));
    }

    @PostMapping("/mfa/setup")
    public ResponseEntity<?> setupMfa(@RequestParam String email) throws Exception {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Error: User is not found."));

        if (user.isMfaEnabled()) {
            return ResponseEntity.badRequest().body(new MessageResponse("MFA is already enabled."));
        }

        String secret = mfaService.generateSecret();
        user.setMfaSecret(secret);
        userRepository.save(user);

        String qrCodeUri = mfaService.getQrCodeImageUri(secret, user.getEmail());

        return ResponseEntity.ok(new MfaSetupResponse(secret, qrCodeUri));
    }

    @PostMapping("/mfa/enable")
    public ResponseEntity<?> enableMfa(@Valid @RequestBody MfaVerificationRequest mfaRequest) {
        User user = userRepository.findByName(mfaRequest.getName())
                .orElseThrow(() -> new RuntimeException("Error: User is not found."));

        boolean isCodeValid = mfaService.verifyCode(user.getMfaSecret(), mfaRequest.getCode());
        if (!isCodeValid) {
            return ResponseEntity.badRequest().body(new MessageResponse("Invalid MFA Code."));
        }

        user.setMfaEnabled(true);
        userRepository.save(user);

        return ResponseEntity.ok(new MessageResponse("MFA Enabled Successfully!"));
    }

    @PostMapping("/mfa/verify")
    public ResponseEntity<?> verifyMfa(@Valid @RequestBody MfaVerificationRequest mfaRequest) {
        User user = userRepository.findByName(mfaRequest.getName())
                .orElseThrow(() -> new RuntimeException("Error: User is not found."));

        boolean isCodeValid = mfaService.verifyCode(user.getMfaSecret(), mfaRequest.getCode());
        if (!isCodeValid) {
            return ResponseEntity.badRequest().body(new MessageResponse("Invalid MFA Code."));
        }

        // Generate JWT token
        String jwt = jwtUtils.generateTokenFromUsername(user.getName());

        return ResponseEntity.ok(new JwtResponse(jwt, user.getId(), user.getName(), 
                user.getEmail(), user.getRole().name(), true));
    }
}
