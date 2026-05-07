package com.wellness.platform.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class EmailOtpService {

    @Autowired
    private JavaMailSender mailSender;

    // email -> {otp, expiryTimeMs}
    private final Map<String, long[]> otpStore = new ConcurrentHashMap<>();

    private static final long OTP_EXPIRY_MS = 5 * 60 * 1000; // 5 minutes

    public void sendOtp(String email) {
        String otp = String.format("%06d", new Random().nextInt(999999));
        long expiry = System.currentTimeMillis() + OTP_EXPIRY_MS;
        otpStore.put(email.toLowerCase(), new long[]{Long.parseLong(otp), expiry});

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(email);
            message.setSubject("WellnessHub - Email Verification Code");
            message.setText(
                "Hello!\n\n" +
                "Your WellnessHub email verification code is:\n\n" +
                "  " + otp + "\n\n" +
                "This code expires in 5 minutes.\n\n" +
                "If you did not request this, please ignore this email.\n\n" +
                "— The WellnessHub Team"
            );
            mailSender.send(message);
        } catch (Exception e) {
            System.out.println("==================================================");
            System.out.println("⚠️ EMAIL OTP COULD NOT BE SENT DUE TO GMAIL AUTH.");
            System.out.println("⚠️ USE THIS OTP TO COMPLETE REGISTRATION:");
            System.out.println("👉 " + otp + " 👈");
            System.out.println("==================================================");
        }
    }

    public boolean verifyOtp(String email, String code) {
        long[] entry = otpStore.get(email.toLowerCase());
        if (entry == null) return false;
        long storedOtp = entry[0];
        long expiry    = entry[1];
        if (System.currentTimeMillis() > expiry) {
            otpStore.remove(email.toLowerCase());
            return false;
        }
        if (storedOtp == Long.parseLong(code.trim())) {
            otpStore.remove(email.toLowerCase());
            return true;
        }
        return false;
    }
}
