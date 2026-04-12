package com.nox.platform.module.iam.service.abstraction;

public interface MailProvider {
    void sendEmail(String to, String subject, String context);
}
