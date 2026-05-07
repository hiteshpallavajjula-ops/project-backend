package com.wellness.platform.service;

import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class SmsService {

    private static final Logger logger = LoggerFactory.getLogger(SmsService.class);

    @Value("${twilio.phone.number}")
    private String fromPhoneNumber;

    /**
     * Sends an OTP SMS to the given phone number via Twilio.
     *
     * @param toPhoneNumber Phone number in E.164 format, e.g. +91XXXXXXXXXX
     * @param otp           The 6-digit OTP to send
     */
    public void sendOtp(String toPhoneNumber, String otp) {
        try {
            Message message = Message.creator(
                    new PhoneNumber(toPhoneNumber),
                    new PhoneNumber(fromPhoneNumber),
                    "Your WellnessHub verification code is: " + otp + ". It expires in 5 minutes. Do not share it with anyone."
            ).create();

            logger.info("SMS sent to {} | SID: {}", toPhoneNumber, message.getSid());
        } catch (Exception e) {
            logger.error("Failed to send SMS to {}: {}", toPhoneNumber, e.getMessage());
            throw new RuntimeException("Failed to send OTP via SMS. Please try again.");
        }
    }
}
