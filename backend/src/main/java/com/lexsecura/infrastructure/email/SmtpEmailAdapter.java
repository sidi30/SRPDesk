package com.lexsecura.infrastructure.email;

import com.lexsecura.application.port.EmailPort;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

@Component
public class SmtpEmailAdapter implements EmailPort {

    private static final Logger log = LoggerFactory.getLogger(SmtpEmailAdapter.class);

    private final JavaMailSender mailSender;
    private final String fromAddress;
    private final boolean enabled;

    public SmtpEmailAdapter(JavaMailSender mailSender,
                            @Value("${app.email.from:noreply@srpdesk.com}") String fromAddress,
                            @Value("${app.email.enabled:false}") boolean enabled) {
        this.mailSender = mailSender;
        this.fromAddress = fromAddress;
        this.enabled = enabled;
    }

    @Override
    public void send(String to, String subject, String htmlBody) {
        if (!enabled) {
            log.info("[EMAIL STUB] To: {}, Subject: {}", to, subject);
            return;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromAddress);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);
            mailSender.send(message);
            log.info("Email sent to {} with subject: {}", to, subject);
        } catch (Exception e) {
            log.error("Failed to send email to {}: {}", to, e.getMessage(), e);
        }
    }

    @Override
    public boolean isAvailable() {
        return enabled;
    }
}
