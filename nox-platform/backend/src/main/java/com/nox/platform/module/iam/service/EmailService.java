package com.nox.platform.module.iam.service;

import com.nox.platform.module.iam.service.abstraction.MailProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    @Value("${app.frontend.url:http://localhost:3000}")
    private String frontendUrl;

    private final MailProvider mailProvider;

    @Async
    public void sendVerificationEmail(String to, String otpCode) {
        String subject = "Nox Platform - Verify Your Email Address";
        String content = "Welcome to Nox Platform! Your verification code is: " + otpCode
                + "\nThis code will expire in 15 minutes.";
        
        mailProvider.sendEmail(to, subject, content);
        log.info("Requested verification email sending to: {}", to);
    }

    @Async
    public void sendPasswordResetEmail(String to, String otpCode) {
        String subject = "Nox Platform - Password Reset Request";
        String content = "We received a request to reset your password. Your reset code is: " + otpCode
                + "\nIf you didn't request this, you can ignore this email.";
        
        mailProvider.sendEmail(to, subject, content);
        log.info("Requested password reset email sending to: {}", to);
    }

    @Async
    public void sendInvitationEmail(String to, String token) {
        String subject = "Nox Platform - You've been invited!";
        String content = "You have been invited to join an organization on Nox Platform.\nClick this link to accept the invitation: "
                        + frontendUrl + "/accept-invite?token=" + token;
        
        mailProvider.sendEmail(to, subject, content);
        log.info("Requested invitation email sending to: {}", to);
    }
}
