package com.nox.platform.module.iam.domain.event;

import com.nox.platform.module.iam.domain.User;
import org.springframework.context.ApplicationEvent;

public class PasswordResetRequestedEvent extends ApplicationEvent {
    private final User user;
    private final String otpCode;

    public PasswordResetRequestedEvent(Object source, User user, String otpCode) {
        super(source);
        this.user = user;
        this.otpCode = otpCode;
    }

    public User getUser() {
        return user;
    }

    public String getOtpCode() {
        return otpCode;
    }
}
