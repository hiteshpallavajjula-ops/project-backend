package com.wellness.platform.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${app.mail.sender:hitesh.pallavajjula@gmail.com}")
    private String fromEmail;

    /**
     * Sends a registration OTP to the given email address.
     *
     * @param toEmail Recipient's email address
     * @param otp     6-digit OTP code
     */
    public void sendOtpEmail(String toEmail, String otp) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("Your WellnessHub Verification Code");
        message.setText(
                "Hello,\n\n" +
                "Your OTP for registration is: " + otp + "\n\n" +
                "This code expires in 5 minutes. Do not share it with anyone.\n\n" +
                "If you did not request this, please ignore this email.\n\n" +
                "— The WellnessHub Team"
        );
        
        // Let it throw the exception so the frontend displays the exact error!
        mailSender.send(message);
        System.out.println("✅ Email sent successfully to " + toEmail);
    }
}
