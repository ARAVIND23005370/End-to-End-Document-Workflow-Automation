package com.project.documentworkflow.service;

import com.project.documentworkflow.dto.UploadResponse;
import com.project.documentworkflow.model.Decision;
import com.project.documentworkflow.model.Document;
import com.project.documentworkflow.model.OCRData;
import com.project.documentworkflow.repository.DocumentRepository;
import com.project.documentworkflow.repository.OCRDataRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Service
public class DocumentWorkflowService {

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private OCRDataRepository ocrDataRepository;

    @Autowired
    private DecisionEngineService decisionEngineService;

    @Autowired
    private AuditService auditService;

    private static final Logger log = LoggerFactory.getLogger(DocumentWorkflowService.class);

    private final String uploadDir = "C:/Users/admin/documentworkflow_uploads/";

    public UploadResponse processUpload(MultipartFile file, String uploaderEmail) throws Exception {

        File folder = new File(uploadDir);
        if (!folder.exists()) {
            folder.mkdirs();
        }

        String filePath = uploadDir + file.getOriginalFilename();
        Path path = Paths.get(filePath);
        Files.write(path, file.getBytes());

        String extractedText = "Sample Extracted Text";

        Document document = new Document();
        document.setDocumentType("PDF");
        document.setFilePath(filePath);
        document.setStatus("UPLOADED");

        // FEATURE: Save who uploaded it (for email on reject)
        document.setUploadedByEmail(uploaderEmail);

        log.info("Starting file upload process for file: {}", file.getOriginalFilename());

        Document savedDocument = documentRepository.save(document);

        // FEATURE: Audit the upload
        auditService.log(savedDocument.getDocumentId(), "UPLOADED", uploaderEmail,
            "File uploaded: " + file.getOriginalFilename());

        OCRData ocrData = new OCRData();
        ocrData.setDocument(savedDocument);
        ocrData.setExtractedText(extractedText);
        ocrData.setConfidenceScore(0.95);

        OCRData savedOcrData = ocrDataRepository.save(ocrData);

        Decision decision = decisionEngineService.evaluateDecision(
                savedDocument.getDocumentId(),
                savedOcrData.getOcrDataId()
        );
        log.info("Decision {} applied for document ID {}", decision.getDecisionType(), savedDocument.getDocumentId());

        UploadResponse response = new UploadResponse();
        response.setDocumentId(savedDocument.getDocumentId());
        response.setDecision(decision.getDecisionType());
        response.setStatus(savedDocument.getStatus());

        return response;
    }
}
