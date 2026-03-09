package com.project.documentworkflow.service;

import com.project.documentworkflow.model.Document;
import com.project.documentworkflow.model.DocumentForward;
import com.project.documentworkflow.repository.DocumentForwardRepository;
import com.project.documentworkflow.repository.DocumentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * DocumentForwardService
 *
 * FEATURE: Send document to any department or person.
 * Supported departments: Finance, HR, Legal, IT, Operations, Management
 */
@Service
public class DocumentForwardService {

    @Autowired
    private DocumentForwardRepository documentForwardRepository;

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private AuditService auditService;

    public DocumentForward forwardDocument(Long documentId, String forwardedBy,
                                           String forwardedTo, String forwardType, String note) {

        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Document not found: " + documentId));

        DocumentForward forward = new DocumentForward();
        forward.setDocument(document);
        forward.setForwardedBy(forwardedBy);
        forward.setForwardedTo(forwardedTo);
        forward.setForwardType(forwardType); // DEPARTMENT or EMAIL
        forward.setNote(note);

        DocumentForward saved = documentForwardRepository.save(forward);

        // Update document department
        document.setDepartment(forwardedTo);
        documentRepository.save(document);

        // Audit this action
        auditService.log(
            documentId,
            "FORWARDED",
            forwardedBy,
            "Document forwarded to " + forwardType + ": " + forwardedTo
                + (note != null ? " | Note: " + note : "")
        );

        System.out.println("[FORWARD] Document " + documentId
                + " forwarded to " + forwardType + ": " + forwardedTo
                + " by " + forwardedBy);

        return saved;
    }

    public List<DocumentForward> getForwardsForDocument(Long documentId) {
        return documentForwardRepository.findByDocument_DocumentId(documentId);
    }

    public List<DocumentForward> getAllForwards() {
        return documentForwardRepository.findAll();
    }
}
