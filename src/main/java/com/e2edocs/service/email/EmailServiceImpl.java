package com.e2edocs.service.email;

import com.e2edocs.exception.BadRequestException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class EmailServiceImpl implements EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailServiceImpl.class);

    private final ObjectProvider<JavaMailSender> mailSenderProvider;

    @Value("${spring.mail.host:${MAIL_HOST:}}")
    private String mailHost;

    @Value("${spring.mail.from:${MAIL_FROM:no-reply@e2edocs.com}}")
    private String fromAddress;

    @Value("${spring.mail.from-name:${MAIL_FROM_NAME:E2EDocs Platform}}")
    private String fromName;

    public EmailServiceImpl(ObjectProvider<JavaMailSender> mailSenderProvider) {
        this.mailSenderProvider = mailSenderProvider;
    }

    @Override
    public void sendEmail(String to, String subjectTemplate, String messageTemplate, Map<String, Object> templateContext) {
        if (to == null || to.isBlank()) {
            logger.warn("Attempted to send rule-based email but recipient address was empty.");
            return;
        }

        String resolvedSubject = resolveTemplate(subjectTemplate, templateContext);
        String resolvedMessage = resolveTemplate(messageTemplate, templateContext);

        JavaMailSender mailSender = mailSenderProvider.getIfAvailable();
        if (mailSender != null && mailHost != null && !mailHost.isBlank()) {
            try {
                MimeMessage mimeMessage = mailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, false, "UTF-8");
                helper.setFrom(fromAddress, fromName);
                helper.setTo(to.trim());
                helper.setSubject(resolvedSubject);
                helper.setText(resolvedMessage, false);

                mailSender.send(mimeMessage);
                logger.info("[SMTP EMAIL SENT] To: {}, Subject: '{}'", to, resolvedSubject);
            } catch (Exception e) {
                logger.error("[SMTP EMAIL FAILED] Could not send email to {}: {}", to, e.getMessage());
            }
        } else {
            logger.info("[EMAIL DISPATCH - SIMULATED] To: {}, Subject: '{}', Body: '{}'", to, resolvedSubject, resolvedMessage);
        }
    }

    @Override
    public void sendDocumentEmail(String to, String subject, String message, String filename, byte[] attachmentBytes, String contentType) {
        if (to == null || to.isBlank()) {
            throw new BadRequestException("Recipient email address cannot be empty.");
        }
        if (!to.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            throw new BadRequestException("Invalid recipient email address format: " + to);
        }
        if (subject == null || subject.isBlank()) {
            throw new BadRequestException("Email subject cannot be empty.");
        }
        if (attachmentBytes == null || attachmentBytes.length == 0) {
            throw new BadRequestException("Cannot send document email with missing or empty attachment.");
        }

        String safeFilename = (filename != null && !filename.isBlank()) ? filename : "document.pdf";
        String bodyText = (message != null && !message.isBlank()) ? message : "Please find the attached document: " + safeFilename;

        JavaMailSender mailSender = mailSenderProvider.getIfAvailable();
        if (mailSender != null && mailHost != null && !mailHost.isBlank()) {
            try {
                MimeMessage mimeMessage = mailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
                helper.setFrom(fromAddress, fromName);
                helper.setTo(to.trim());
                helper.setSubject(subject.trim());
                helper.setText(bodyText, false);

                ByteArrayResource attachmentResource = new ByteArrayResource(attachmentBytes);
                String mimeType = (contentType != null && !contentType.isBlank()) ? contentType : "application/octet-stream";
                helper.addAttachment(safeFilename, attachmentResource, mimeType);

                mailSender.send(mimeMessage);
                logger.info("[SMTP DOCUMENT EMAIL SENT] To: {}, Subject: '{}', Attachment: {} ({} bytes)",
                        to, subject, safeFilename, attachmentBytes.length);
            } catch (Exception e) {
                logger.error("[SMTP DOCUMENT EMAIL FAILED] Error sending to {}: {}", to, e.getMessage(), e);
                throw new BadRequestException("Failed to send email via SMTP: " + e.getMessage());
            }
        } else {
            logger.info("[DOCUMENT EMAIL DISPATCH - SIMULATED] To: {}, Subject: '{}', Attachment: {} ({} bytes)",
                    to, subject, safeFilename, attachmentBytes.length);
        }
    }

    private String resolveTemplate(String template, Map<String, Object> context) {
        if (template == null || context == null) {
            return template != null ? template : "";
        }

        String result = template;
        for (Map.Entry<String, Object> entry : context.entrySet()) {
            String placeholderDouble = "{{" + entry.getKey() + "}}";
            String placeholderSingle = "{" + entry.getKey() + "}";
            String val = entry.getValue() != null ? String.valueOf(entry.getValue()) : "";
            result = result.replace(placeholderDouble, val).replace(placeholderSingle, val);
        }
        return result;
    }
}
