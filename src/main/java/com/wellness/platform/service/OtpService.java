package com.wellness.platform.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Service
public class OtpService {

    // email -> otp
    private final Map<String, String> otpStore = new HashMap<>();

    @Autowired
    private EmailService emailService;

    /**
     * Generates a random 6-digit OTP.
     */
    public String generateOtp() {
        return String.format("%06d", new Random().nextInt(999999));
    }

    /**
     * Saves the OTP for the given email in memory.
     */
    public void saveOtp(String email, String otp) {
        otpStore.put(email.toLowerCase(), otp);
    }

    /**
     * Validates the entered OTP against the stored one.
     * Removes the OTP from the store on successful validation.
     *
     * @return true if OTP matches, false otherwise
     */
    public boolean validateOtp(String email, String enteredOtp) {
        String stored = otpStore.get(email.toLowerCase());
        if (stored != null && stored.equals(enteredOtp.trim())) {
            otpStore.remove(email.toLowerCase());
            return true;
        }
        return false;
    }

    /**
     * Generates OTP, stores it, and sends it to the given email.
     */
    public void generateAndSendOtp(String email) {
        String otp = generateOtp();
        saveOtp(email, otp);
        emailService.sendOtpEmail(email, otp);
    }
}
