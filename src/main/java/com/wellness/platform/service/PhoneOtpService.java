package com.wellness.platform.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class PhoneOtpService {

    // phone -> {otp, expiryTimeMs}
    private final Map<String, long[]> otpStore = new ConcurrentHashMap<>();

    private static final long OTP_EXPIRY_MS = 5 * 60 * 1000; // 5 minutes

    @Autowired
    private SmsService smsService;

    public void sendOtp(String phone) {
        String normalizedPhone = normalizePhone(phone);
        String otp = String.format("%06d", new Random().nextInt(999999));
        long expiry = System.currentTimeMillis() + OTP_EXPIRY_MS;
        otpStore.put(normalizedPhone, new long[]{Long.parseLong(otp), expiry});

        // Send real SMS via Twilio
        smsService.sendOtp(normalizedPhone, otp);
    }

    public boolean verifyOtp(String phone, String code) {
        String normalizedPhone = normalizePhone(phone);
        long[] entry = otpStore.get(normalizedPhone);
        if (entry == null) return false;
        long storedOtp = entry[0];
        long expiry    = entry[1];
        if (System.currentTimeMillis() > expiry) {
            otpStore.remove(normalizedPhone);
            return false;
        }
        if (storedOtp == Long.parseLong(code.trim())) {
            otpStore.remove(normalizedPhone);
            return true;
        }
        return false;
    }

    /** Strips spaces and dashes but keeps the leading '+' */
    private String normalizePhone(String phone) {
        if (phone == null) return "";
        return phone.trim().replaceAll("[\\s\\-]", "");
    }
}
