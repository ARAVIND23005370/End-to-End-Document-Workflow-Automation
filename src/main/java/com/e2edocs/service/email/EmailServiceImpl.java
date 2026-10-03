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

    @Override
    public void sendInvitationEmail(String to, String inviteeName, String inviterName, String inviterEmail, String orgName, String role, String setupUrl) {
        if (to == null || to.isBlank()) {
            logger.warn("Cannot send invitation email: recipient address is blank.");
            return;
        }

        String safeInvitee = (inviteeName != null && !inviteeName.isBlank()) ? inviteeName : "Colleague";
        String safeInviterName = (inviterName != null && !inviterName.isBlank()) ? inviterName : "Administrator";
        String safeInviterEmail = (inviterEmail != null && !inviterEmail.isBlank()) ? inviterEmail : fromAddress;
        String safeOrg = (orgName != null && !orgName.isBlank()) ? orgName : "E2EDocs Workspace";
        String safeRole = (role != null && !role.isBlank()) ? role.toUpperCase() : "MEMBER";

        String subject = String.format("Invitation to join %s on E2EDocs from %s", safeOrg, safeInviterName);

        String plainText = String.format(
                "Hello %s,\n\n" +
                "%s (%s) has invited you to join the organization \"%s\" on E2EDocs with the role of %s.\n\n" +
                "To accept this invitation and activate your account, please click the link below to set your password:\n\n" +
                "%s\n\n" +
                "This invitation link will expire in 72 hours.\n\n" +
                "Best regards,\n" +
                "The E2EDocs Team\n" +
                "Developed by AravindRamesh",
                safeInvitee, safeInviterName, safeInviterEmail, safeOrg, safeRole, setupUrl
        );

        String htmlText = String.format(
                "<!DOCTYPE html>" +
                "<html><head><meta charset='UTF-8'><meta name='viewport' content='width=device-width, initial-scale=1.0'></head>" +
                "<body style='margin:0;padding:0;background-color:#f8fafc;font-family:-apple-system,BlinkMacSystemFont,\"Segoe UI\",Roboto,Helvetica,Arial,sans-serif;color:#1e293b;'>" +
                "<div style='max-width:560px;margin:32px auto;background:#ffffff;border-radius:12px;overflow:hidden;box-shadow:0 4px 6px -1px rgba(0,0,0,0.05), 0 2px 4px -1px rgba(0,0,0,0.03);border:1px solid #e2e8f0;'>" +
                "  <div style='background:linear-gradient(135deg, #4f46e5 0%%, #6366f1 100%%);padding:28px 32px;text-align:center;color:#ffffff;'>" +
                "    <h1 style='margin:0;font-size:24px;font-weight:700;letter-spacing:-0.5px;'>E2EDocs</h1>" +
                "    <p style='margin:6px 0 0 0;font-size:14px;opacity:0.9;'>End-to-End Document Workflow Automation</p>" +
                "  </div>" +
                "  <div style='padding:32px;'>" +
                "    <h2 style='margin:0 0 16px 0;font-size:18px;color:#0f172a;font-weight:600;'>You've been invited to join %s</h2>" +
                "    <p style='font-size:15px;line-height:1.6;color:#334155;margin:0 0 16px 0;'>Hello <strong>%s</strong>,</p>" +
                "    <p style='font-size:15px;line-height:1.6;color:#334155;margin:0 0 20px 0;'>" +
                "      <strong>%s</strong> (<a href='mailto:%s' style='color:#4f46e5;text-decoration:none;'>%s</a>) has invited you to join the " +
                "      <strong>%s</strong> team on E2EDocs with the role of <span style='display:inline-block;padding:2px 8px;background:#eef2ff;color:#4338ca;border-radius:4px;font-weight:600;font-size:13px;'>%s</span>." +
                "    </p>" +
                "    <div style='text-align:center;margin:28px 0;'>" +
                "      <a href='%s' style='display:inline-block;background-color:#4f46e5;color:#ffffff;text-decoration:none;font-weight:600;font-size:15px;padding:12px 28px;border-radius:6px;box-shadow:0 2px 4px rgba(79,70,229,0.3);'>Accept Invite & Set Password</a>" +
                "    </div>" +
                "    <p style='font-size:13px;line-height:1.5;color:#64748b;margin:24px 0 0 0;'>If the button above does not work, copy and paste this link into your browser:</p>" +
                "    <p style='font-size:12px;color:#4f46e5;word-break:break-all;margin:6px 0 0 0;'><a href='%s' style='color:#4f46e5;'>%s</a></p>" +
                "    <div style='margin-top:28px;padding-top:20px;border-top:1px solid #f1f5f9;font-size:12px;color:#94a3b8;'>" +
                "      <p style='margin:0;'>This invitation link will expire in 72 hours. If you were not expecting this invite, please disregard this email.</p>" +
                "    </div>" +
                "  </div>" +
                "  <div style='background:#f8fafc;padding:16px 32px;text-align:center;font-size:12px;color:#94a3b8;border-top:1px solid #e2e8f0;'>" +
                "    &copy; 2026 E2EDocs &bull; Developed by AravindRamesh" +
                "  </div>" +
                "</div>" +
                "</body></html>",
                safeOrg, safeInvitee, safeInviterName, safeInviterEmail, safeInviterEmail, safeOrg, safeRole, setupUrl, setupUrl, setupUrl
        );

        sendDispatch(to, safeInviterEmail, safeInviterName, subject, plainText, htmlText);
    }

    @Override
    public void sendPasswordResetEmail(String to, String userName, String resetUrl) {
        if (to == null || to.isBlank()) {
            logger.warn("Cannot send password reset email: recipient address is blank.");
            return;
        }

        String safeName = (userName != null && !userName.isBlank()) ? userName : "User";
        String subject = "Reset your E2EDocs password";

        String plainText = String.format(
                "Hello %s,\n\n" +
                "We received a request to reset your password for your E2EDocs account.\n\n" +
                "Please click the link below to set a new password:\n\n" +
                "%s\n\n" +
                "This password reset link will expire in 24 hours.\n\n" +
                "If you did not request a password reset, you can safely ignore this email. Your password will remain unchanged.\n\n" +
                "Best regards,\n" +
                "The E2EDocs Security Team\n" +
                "Developed by AravindRamesh",
                safeName, resetUrl
        );

        String htmlText = String.format(
                "<!DOCTYPE html>" +
                "<html><head><meta charset='UTF-8'><meta name='viewport' content='width=device-width, initial-scale=1.0'></head>" +
                "<body style='margin:0;padding:0;background-color:#f8fafc;font-family:-apple-system,BlinkMacSystemFont,\"Segoe UI\",Roboto,Helvetica,Arial,sans-serif;color:#1e293b;'>" +
                "<div style='max-width:560px;margin:32px auto;background:#ffffff;border-radius:12px;overflow:hidden;box-shadow:0 4px 6px -1px rgba(0,0,0,0.05), 0 2px 4px -1px rgba(0,0,0,0.03);border:1px solid #e2e8f0;'>" +
                "  <div style='background:linear-gradient(135deg, #4f46e5 0%%, #6366f1 100%%);padding:28px 32px;text-align:center;color:#ffffff;'>" +
                "    <h1 style='margin:0;font-size:24px;font-weight:700;letter-spacing:-0.5px;'>E2EDocs</h1>" +
                "    <p style='margin:6px 0 0 0;font-size:14px;opacity:0.9;'>Security Notification</p>" +
                "  </div>" +
                "  <div style='padding:32px;'>" +
                "    <h2 style='margin:0 0 16px 0;font-size:18px;color:#0f172a;font-weight:600;'>Password Reset Request</h2>" +
                "    <p style='font-size:15px;line-height:1.6;color:#334155;margin:0 0 16px 0;'>Hello <strong>%s</strong>,</p>" +
                "    <p style='font-size:15px;line-height:1.6;color:#334155;margin:0 0 20px 0;'>" +
                "      We received a request to reset the password associated with your E2EDocs account (<strong>%s</strong>). " +
                "      Click the button below to choose a new password:" +
                "    </p>" +
                "    <div style='text-align:center;margin:28px 0;'>" +
                "      <a href='%s' style='display:inline-block;background-color:#4f46e5;color:#ffffff;text-decoration:none;font-weight:600;font-size:15px;padding:12px 28px;border-radius:6px;box-shadow:0 2px 4px rgba(79,70,229,0.3);'>Reset Password</a>" +
                "    </div>" +
                "    <p style='font-size:13px;line-height:1.5;color:#64748b;margin:24px 0 0 0;'>If the button above does not work, copy and paste this link into your browser:</p>" +
                "    <p style='font-size:12px;color:#4f46e5;word-break:break-all;margin:6px 0 0 0;'><a href='%s' style='color:#4f46e5;'>%s</a></p>" +
                "    <div style='margin-top:28px;padding-top:20px;border-top:1px solid #f1f5f9;font-size:12px;color:#94a3b8;'>" +
                "      <p style='margin:0;'>This link is valid for 24 hours. If you did not make this request, you can safely ignore this email; your account remains secure.</p>" +
                "    </div>" +
                "  </div>" +
                "  <div style='background:#f8fafc;padding:16px 32px;text-align:center;font-size:12px;color:#94a3b8;border-top:1px solid #e2e8f0;'>" +
                "    &copy; 2026 E2EDocs &bull; Developed by AravindRamesh" +
                "  </div>" +
                "</div>" +
                "</body></html>",
                safeName, to, resetUrl, resetUrl, resetUrl
        );

        sendDispatch(to, fromAddress, fromName, subject, plainText, htmlText);
    }

    private void sendDispatch(String to, String replyToEmail, String replyToName, String subject, String plainText, String htmlText) {
        JavaMailSender mailSender = mailSenderProvider.getIfAvailable();
        if (mailSender != null && mailHost != null && !mailHost.isBlank()) {
            try {
                MimeMessage mimeMessage = mailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
                helper.setFrom(fromAddress, fromName);
                if (replyToEmail != null && !replyToEmail.isBlank() && !replyToEmail.equalsIgnoreCase(fromAddress)) {
                    helper.setReplyTo(replyToEmail, replyToName);
                }
                helper.setTo(to.trim());
                helper.setSubject(subject);
                helper.setText(plainText, htmlText);

                mailSender.send(mimeMessage);
                logger.info("[SMTP AUTH EMAIL SENT] To: {}, Subject: '{}'", to, subject);
            } catch (Exception e) {
                logger.error("[SMTP AUTH EMAIL FAILED] Could not send email to {}: {}", to, e.getMessage(), e);
            }
        } else {
            logger.info("[AUTH EMAIL DISPATCH - SIMULATED / CONSOLE]\nTo: {}\nFrom: {} <{}>\nReply-To: {} <{}>\nSubject: {}\n\n{}",
                    to, fromName, fromAddress, replyToName, replyToEmail, subject, plainText);
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
