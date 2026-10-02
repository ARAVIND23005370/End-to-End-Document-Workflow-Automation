package com.e2edocs.service.email;

import java.util.Map;

public interface EmailService {
    /**
     * Send template-based email (e.g. for automated Rule Engine SEND_EMAIL action).
     */
    void sendEmail(String to, String subjectTemplate, String messageTemplate, Map<String, Object> templateContext);

    /**
     * Send manual document delivery email with original document attachment.
     */
    void sendDocumentEmail(String to, String subject, String message, String filename, byte[] attachmentBytes, String contentType);
}
