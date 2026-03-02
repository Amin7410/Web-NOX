package com.nox.platform.module.iam.service;

import org.junit.jupiter.api.Test;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;
import static org.mockito.Mockito.mock;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class EmailServiceTest {

    @Test
    void sendVerificationEmail_doesNotThrow() {
        JavaMailSender mailSender = mock(JavaMailSender.class);
        EmailService emailService = new EmailService(mailSender);
        ReflectionTestUtils.setField(emailService, "frontendUrl", "http://localhost:3000");

        assertDoesNotThrow(() -> emailService.sendVerificationEmail("test@nox.com", "123456"));
    }

    @Test
    void sendPasswordResetEmail_doesNotThrow() {
        JavaMailSender mailSender = mock(JavaMailSender.class);
        EmailService emailService = new EmailService(mailSender);
        ReflectionTestUtils.setField(emailService, "frontendUrl", "http://localhost:3000");

        assertDoesNotThrow(() -> emailService.sendPasswordResetEmail("test@nox.com", "123456"));
    }

    @Test
    void sendInvitationEmail_doesNotThrow() {
        JavaMailSender mailSender = mock(JavaMailSender.class);
        EmailService emailService = new EmailService(mailSender);
        ReflectionTestUtils.setField(emailService, "frontendUrl", "http://localhost:3000");

        assertDoesNotThrow(() -> emailService.sendInvitationEmail("test@nox.com", "dummy-verify-token"));
    }
}
