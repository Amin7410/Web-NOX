package com.nox.platform.module.iam.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;

@Slf4j
@Service
public class EmailService {

    @Value("${app.frontend.url:http://localhost:3000}")
    private String frontendUrl;

    @Value("${spring.mail.username:noreply@nox-platform.com}")
    private String fromEmail;

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Async
    public void sendVerificationEmail(String to, String otpCode) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(to);
        message.setSubject("Nox Platform - Verify Your Email Address");
        message.setText("Welcome to Nox Platform! Your verification code is: " + otpCode
                + "\nThis code will expire in 15 minutes.");
        mailSender.send(message);
        log.info("Sent verification email to: {}", to);
    }

    @Async
    public void sendPasswordResetEmail(String to, String otpCode) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(to);
        message.setSubject("Nox Platform - Password Reset Request");
        message.setText("We received a request to reset your password. Your reset code is: " + otpCode
                + "\nIf you didn't request this, you can ignore this email.");
        mailSender.send(message);
        log.info("Sent password reset email to: {}", to);
    }

    @Async
    public void sendInvitationEmail(String to, String token) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(to);
        message.setSubject("Nox Platform - You've been invited!");
        message.setText(
                "You have been invited to join an organization on Nox Platform.\nClick this link to accept the invitation: "
                        + frontendUrl + "/accept-invite?token=" + token);
        mailSender.send(message);
        log.info("Sent invitation email to: {}", to);
    }
}
