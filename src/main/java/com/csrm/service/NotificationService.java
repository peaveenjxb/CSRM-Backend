package com.csrm.service;

import com.csrm.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {
    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);
    private final ObjectProvider<JavaMailSender> mail;
    private final String from;

    public NotificationService(ObjectProvider<JavaMailSender> mail, @Value("${spring.mail.username:}") String from) {
        this.mail = mail;
        this.from = from;
    }

    public void notify(User u, String subject, String text) {
        log.info("[NOTIFY] {} -> {}: {}", u.username, subject, text); // always visible in the console
        JavaMailSender sender = mail.getIfAvailable();
        if (sender == null || u.email == null || u.email.isBlank())
            return;
        try {
            SimpleMailMessage m = new SimpleMailMessage();
            if (from != null && !from.isBlank()) {
                m.setFrom(from);
            }
            m.setTo(u.email);
            m.setSubject("CSRM: " + subject);
            m.setText(text);
            sender.send(m);
        } catch (Exception e) {
            log.warn("Email failed for {}: {}", u.username, e.getMessage());
        }
        // SMS: call your SMS provider (e.g. Twilio) here
    }
}
