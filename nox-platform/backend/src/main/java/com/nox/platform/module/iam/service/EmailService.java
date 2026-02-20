package com.nox.platform.module.iam.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class EmailService {

    @Async
    public void sendVerificationEmail(String to, String otpCode) {
        log.info("=========================================");
        log.info("Sending Email Verification to: {}", to);
        log.info("Here is your OTP code to verify your account: {}", otpCode);
        log.info("=========================================");
    }

    @Async
    public void sendPasswordResetEmail(String to, String otpCode) {
        log.info("=========================================");
        log.info("Sending Password Reset Email to: {}", to);
        log.info("Here is your OTP code to reset your password: {}", otpCode);
        log.info("=========================================");
    }

    @Async
    public void sendInvitationEmail(String to, String token) {
        log.info("=========================================");
        log.info("Sending Invitation Email to: {}", to);
        log.info("Click this link to accept the invitation: frontend.com/accept-invite?token={}", token);
        log.info("=========================================");
    }
}
