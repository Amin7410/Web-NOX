package com.nox.platform.module.iam.infrastructure.mail;

import com.nox.platform.module.iam.service.abstraction.MailProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SmtpMailProvider implements MailProvider {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:noreply@nox-platform.com}")
    private String fromEmail;

    @Override
    public void sendEmail(String to, String subject, String context) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(context);
            mailSender.send(message);
            log.info("Successfully sent SMTP email to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send SMTP email to: {}. Error: {}", to, e.getMessage());
        }
    }
}
