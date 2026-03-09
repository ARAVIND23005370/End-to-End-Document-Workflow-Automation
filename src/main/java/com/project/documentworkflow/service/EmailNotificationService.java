package com.project.documentworkflow.service;

import com.project.documentworkflow.model.Document;
import com.project.documentworkflow.model.User;
import com.project.documentworkflow.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * EmailNotificationService
 *
 * FEATURE: Auto-send email when document is REJECTED — but ONLY if user gave permission.
 *
 * NOTE FOR STUDENTS:
 * Right now this service SIMULATES sending an email (prints to console log).
 * To make real emails work, you need to:
 *   1. Add spring-boot-starter-mail to pom.xml
 *   2. Set spring.mail.host, spring.mail.username, spring.mail.password in application.properties
 *   3. Inject JavaMailSender and use it here
 */
@Service
public class EmailNotificationService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuditService auditService;

    /**
     * Called automatically when a document is REJECTED.
     * Checks if user allowed email notification before sending.
     */
    public void sendRejectionEmailIfAllowed(Document document, String reason) {
        String uploaderEmail = document.getUploadedByEmail();

        if (uploaderEmail == null || uploaderEmail.isBlank()) {
            System.out.println("[EMAIL] No uploader email on document " + document.getDocumentId() + ", skipping.");
            return;
        }

        User user = userRepository.findByEmail(uploaderEmail).orElse(null);

        if (user == null) {
            System.out.println("[EMAIL] User not found for email: " + uploaderEmail);
            return;
        }

        // Check user permission — only send if they opted in
        if (Boolean.TRUE.equals(user.getEmailNotifyOnReject())) {
            // === REAL EMAIL CODE GOES HERE ===
            // Example with JavaMailSender:
            // SimpleMailMessage msg = new SimpleMailMessage();
            // msg.setTo(user.getEmail());
            // msg.setSubject("Your Document Was Rejected");
            // msg.setText("Dear " + user.getName() + ",\n\nYour document (ID: "
            //   + document.getDocumentId() + ") was rejected.\nReason: " + reason);
            // mailSender.send(msg);

            System.out.println("[EMAIL SENT] To: " + user.getEmail()
                    + " | Subject: Document Rejected"
                    + " | DocumentId: " + document.getDocumentId()
                    + " | Reason: " + reason);

            // Audit the email send
            auditService.log(
                document.getDocumentId(),
                "EMAIL_SENT",
                "SYSTEM",
                "Rejection email sent to " + user.getEmail() + " | Reason: " + reason
            );

        } else {
            System.out.println("[EMAIL] User " + uploaderEmail + " has NOT opted in for email notifications. Skipping.");
        }
    }
}
