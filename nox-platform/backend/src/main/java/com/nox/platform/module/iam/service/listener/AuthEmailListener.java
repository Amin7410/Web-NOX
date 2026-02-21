package com.nox.platform.module.iam.service.listener;

import com.nox.platform.module.iam.domain.event.PasswordResetRequestedEvent;
import com.nox.platform.module.iam.domain.event.UserRegisteredEvent;
import com.nox.platform.module.iam.service.EmailService;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class AuthEmailListener {

    private final EmailService emailService;

    public AuthEmailListener(EmailService emailService) {
        this.emailService = emailService;
    }

    @EventListener
    public void handleUserRegisteredEvent(UserRegisteredEvent event) {
        emailService.sendVerificationEmail(event.getUser().getEmail(), event.getOtpCode());
    }

    @EventListener
    public void handlePasswordResetRequestedEvent(PasswordResetRequestedEvent event) {
        emailService.sendPasswordResetEmail(event.getUser().getEmail(), event.getOtpCode());
    }
}
