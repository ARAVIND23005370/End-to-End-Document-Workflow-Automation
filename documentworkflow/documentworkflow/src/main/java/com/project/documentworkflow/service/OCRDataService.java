package com.project.documentworkflow.service;

import com.project.documentworkflow.dto.UploadResponse;
import com.project.documentworkflow.model.Decision;
import com.project.documentworkflow.model.Document;
import com.project.documentworkflow.model.OCRData;
import com.project.documentworkflow.repository.DocumentRepository;
import com.project.documentworkflow.repository.OCRDataRepository;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class DocumentWorkflowService {

    @Autowired private DocumentRepository documentRepository;
    @Autowired private OCRDataRepository ocrDataRepository;
    @Autowired private DecisionEngineService decisionEngineService;
    @Autowired private AuditService auditService;

    private static final Logger log = LoggerFactory.getLogger(DocumentWorkflowService.class);
    private final String uploadDir = "C:/Users/admin/documentworkflow_uploads/";

    public UploadResponse processUpload(MultipartFile file, String uploaderEmail) throws Exception {

        // Create upload folder if not exists
        File folder = new File(uploadDir);
        if (!folder.exists()) folder.mkdirs();

        // Save file to disk
        String filePath = uploadDir + file.getOriginalFilename();
        Files.write(Paths.get(filePath), file.getBytes());

        log.info("Starting upload for file: {}", file.getOriginalFilename());

        // ── Read file content ──────────────────────────────────────────────
        String fileContent = "";
        try {
            String fn = file.getOriginalFilename() != null ?
                    file.getOriginalFilename().toLowerCase() : "";

            if (fn.endsWith(".pdf")) {
                PDDocument pdDoc = PDDocument.load(file.getInputStream());
                PDFTextStripper stripper = new PDFTextStripper();
                fileContent = stripper.getText(pdDoc).toLowerCase();
                pdDoc.close();
            } else {
                fileContent = new String(file.getBytes(), "UTF-8").toLowerCase();
            }
        } catch (Exception e) {
            log.warn("Could not read file content: {}", e.getMessage());
            fileContent = "";
        }

        String fileName = file.getOriginalFilename() != null ?
                file.getOriginalFilename().toLowerCase() : "";

        // ── Detect document type first ─────────────────────────────────────
        String documentType = detectDocumentType(fileContent, fileName);

        // ── Calculate confidence based on document type ────────────────────
        double confidenceScore = calculateConfidence(fileContent, fileName, documentType);

        String extractedText = fileContent.length() > 500 ?
                fileContent.substring(0, 500) : fileContent;
        if (extractedText.trim().isEmpty())
            extractedText = "No readable text extracted from: " + file.getOriginalFilename();

        log.info("Type: {} | Confidence: {}% | File: {}",
                documentType, String.format("%.0f", confidenceScore * 100), fileName);

        // ── Save Document ──────────────────────────────────────────────────
        Document document = new Document();
        document.setDocumentType(documentType);
        document.setFilePath(filePath);
        document.setStatus("UPLOADED");
        document.setUploadedByEmail(uploaderEmail);
        Document savedDocument = documentRepository.save(document);

        auditService.log(savedDocument.getDocumentId(), "UPLOADED", uploaderEmail,
                "File: " + file.getOriginalFilename() +
                " | Type: " + documentType +
                " | Confidence: " + String.format("%.0f", confidenceScore * 100) + "%");

        // ── Save OCR Data ──────────────────────────────────────────────────
        OCRData ocrData = new OCRData();
        ocrData.setDocument(savedDocument);
        ocrData.setExtractedText(extractedText);
        ocrData.setConfidenceScore(confidenceScore);
        OCRData savedOcrData = ocrDataRepository.save(ocrData);

        // ── Run Decision Engine ────────────────────────────────────────────
        Decision decision = decisionEngineService.evaluateDecision(
                savedDocument.getDocumentId(), savedOcrData.getOcrDataId());

        log.info("Decision: {} | Doc ID: {} | Confidence: {}%",
                decision.getDecisionType(), savedDocument.getDocumentId(),
                String.format("%.0f", confidenceScore * 100));

        UploadResponse response = new UploadResponse();
        response.setDocumentId(savedDocument.getDocumentId());
        response.setDecision(decision.getDecisionType());
        response.setStatus(savedDocument.getStatus());
        return response;
    }

    // ══════════════════════════════════════════════════════════════════════════
    // CONFIDENCE CALCULATION — Different rules per document type
    // ══════════════════════════════════════════════════════════════════════════
    private double calculateConfidence(String content, String fileName, String documentType) {

        if (content == null || content.trim().length() < 20)
            return 0.08 + (Math.random() * 0.05);

        int score = 0;
        int total = 10;

        switch (documentType) {

            case "LOAN":
                if (content.contains("name") || content.contains("applicant")) score++;
                if (content.matches(".*\\d{2}[/\\-]\\d{2}[/\\-]\\d{4}.*")) score++;
                if (content.contains("pan") || content.contains("aadhaar")) score++;
                if (content.contains("income") || content.contains("salary")) score++;
                if (content.contains("loan amount") || content.contains("amount")) score++;
                if (content.contains("mobile") || content.contains("phone") || content.contains("email")) score++;
                if (content.contains("address") || content.contains("chennai") || content.contains("nagar")) score++;
                if (content.contains("signature") || content.contains("sign")) score++;
                if (content.contains("tenure") || content.contains("purpose")) score++;
                if (content.length() > 300) score++;
                break;

            case "COMPLAINT":
                if (content.contains("name") || content.contains("complainant")) score++;
                if (content.matches(".*\\d{2}[/\\-]\\d{2}[/\\-]\\d{4}.*")) score++;
                if (content.contains("complaint") || content.contains("issue") || content.contains("problem")) score++;
                if (content.contains("account") || content.contains("reference")) score++;
                if (content.contains("branch") || content.contains("address")) score++;
                if (content.contains("mobile") || content.contains("phone") || content.contains("email")) score++;
                if (content.contains("action") || content.contains("request") || content.contains("resolution")) score++;
                if (content.contains("signature") || content.contains("sign")) score++;
                if (content.contains("amount") || content.contains("rs.") || content.contains("rupees")) score++;
                if (content.length() > 200) score++;
                break;

            case "INVOICE":
                if (content.contains("invoice") || content.contains("bill")) score++;
                if (content.matches(".*\\d{2}[/\\-]\\d{2}[/\\-]\\d{4}.*")) score++;
                if (content.contains("gst") || content.contains("tax")) score++;
                if (content.contains("amount") || content.contains("total") || content.contains("rs.")) score++;
                if (content.contains("vendor") || content.contains("supplier") || content.contains("from")) score++;
                if (content.contains("address") || content.contains("city")) score++;
                if (content.contains("payment") || content.contains("due")) score++;
                if (content.contains("item") || content.contains("description") || content.contains("qty")) score++;
                if (content.contains("signature") || content.contains("authorised")) score++;
                if (content.length() > 300) score++;
                break;

            case "MEDICAL":
                if (content.contains("patient") || content.contains("name")) score++;
                if (content.matches(".*\\d{2}[/\\-]\\d{2}[/\\-]\\d{4}.*")) score++;
                if (content.contains("doctor") || content.contains("physician") || content.contains("dr.")) score++;
                if (content.contains("diagnosis") || content.contains("disease") || content.contains("condition")) score++;
                if (content.contains("hospital") || content.contains("clinic") || content.contains("ward")) score++;
                if (content.contains("prescription") || content.contains("medicine") || content.contains("treatment")) score++;
                if (content.contains("blood") || content.contains("report") || content.contains("test")) score++;
                if (content.contains("age") || content.contains("dob") || content.contains("gender")) score++;
                if (content.contains("signature") || content.contains("sign")) score++;
                if (content.length() > 200) score++;
                break;

            case "STUDENT":
                if (content.contains("student") || content.contains("name")) score++;
                if (content.matches(".*\\d{2}[/\\-]\\d{2}[/\\-]\\d{4}.*")) score++;
                if (content.contains("register") || content.contains("roll") || content.contains("id")) score++;
                if (content.contains("college") || content.contains("university") || content.contains("school")) score++;
                if (content.contains("course") || content.contains("department") || content.contains("branch")) score++;
                if (content.contains("grade") || content.contains("marks") || content.contains("gpa") || content.contains("cgpa")) score++;
                if (content.contains("year") || content.contains("semester") || content.contains("batch")) score++;
                if (content.contains("principal") || content.contains("dean") || content.contains("hod")) score++;
                if (content.contains("certificate") || content.contains("transcript") || content.contains("result")) score++;
                if (content.length() > 200) score++;
                break;

            case "HR":
                if (content.contains("employee") || content.contains("name") || content.contains("staff")) score++;
                if (content.matches(".*\\d{2}[/\\-]\\d{2}[/\\-]\\d{4}.*")) score++;
                if (content.contains("designation") || content.contains("position") || content.contains("role")) score++;
                if (content.contains("department") || content.contains("division")) score++;
                if (content.contains("salary") || content.contains("ctc") || content.contains("compensation")) score++;
                if (content.contains("joining") || content.contains("doj")) score++;
                if (content.contains("hr") || content.contains("human resource") || content.contains("manager")) score++;
                if (content.contains("pan") || content.contains("aadhaar") || content.contains("pf")) score++;
                if (content.contains("signature") || content.contains("sign") || content.contains("authorised")) score++;
                if (content.length() > 200) score++;
                break;

            case "LEGAL":
                if (content.contains("party") || content.contains("plaintiff") || content.contains("defendant")) score++;
                if (content.matches(".*\\d{2}[/\\-]\\d{2}[/\\-]\\d{4}.*")) score++;
                if (content.contains("court") || content.contains("tribunal") || content.contains("judge")) score++;
                if (content.contains("case") || content.contains("petition") || content.contains("suit")) score++;
                if (content.contains("advocate") || content.contains("lawyer") || content.contains("attorney")) score++;
                if (content.contains("clause") || content.contains("section") || content.contains("article")) score++;
                if (content.contains("hereby") || content.contains("whereas") || content.contains("agreement")) score++;
                if (content.contains("witness") || content.contains("notary") || content.contains("stamp")) score++;
                if (content.contains("signature") || content.contains("sign")) score++;
                if (content.length() > 300) score++;
                break;

            case "CONTRACT":
                if (content.contains("party") || content.contains("parties")) score++;
                if (content.matches(".*\\d{2}[/\\-]\\d{2}[/\\-]\\d{4}.*")) score++;
                if (content.contains("agreement") || content.contains("contract")) score++;
                if (content.contains("terms") || content.contains("conditions")) score++;
                if (content.contains("payment") || content.contains("amount") || content.contains("consideration")) score++;
                if (content.contains("duration") || content.contains("period") || content.contains("effective")) score++;
                if (content.contains("clause") || content.contains("section")) score++;
                if (content.contains("witness") || content.contains("notary")) score++;
                if (content.contains("signature") || content.contains("sign")) score++;
                if (content.length() > 300) score++;
                break;

            case "IDENTITY":
                if (content.contains("name")) score++;
                if (content.matches(".*\\d{2}[/\\-]\\d{2}[/\\-]\\d{4}.*")) score++;
                if (content.contains("aadhaar") || content.contains("pan") || content.contains("passport")) score++;
                if (content.contains("dob") || content.contains("date of birth") || content.contains("born")) score++;
                if (content.contains("address") || content.contains("residence")) score++;
                if (content.contains("gender") || content.contains("male") || content.contains("female")) score++;
                if (content.contains("photo") || content.contains("photograph")) score++;
                if (content.contains("nationality") || content.contains("citizen")) score++;
                if (content.contains("issued") || content.contains("valid") || content.contains("expiry")) score++;
                if (content.length() > 100) score++;
                break;

            default:
                if (content.contains("name") || content.contains("applicant")) score++;
                if (content.matches(".*\\d{2}[/\\-]\\d{2}[/\\-]\\d{4}.*")) score++;
                if (content.matches(".*\\d+.*")) score++;
                if (content.contains("address") || content.contains("city")) score++;
                if (content.contains("mobile") || content.contains("email") || content.contains("@")) score++;
                if (content.contains("signature") || content.contains("sign")) score++;
                if (content.contains("reference") || content.contains("id") || content.contains("number")) score++;
                if (content.contains("form") || content.contains("application") || content.contains("request")) score++;
                long nonSpace = content.chars().filter(c -> c != ' ' && c != '\n').count();
                if (nonSpace > 150) score++;
                if (content.length() > 200) score++;
                break;
        }

        double raw = (double) score / total;
        double variation = (Math.random() * 0.06) - 0.03;
        double finalScore = Math.min(1.0, Math.max(0.05, raw + variation));

        log.info("Confidence [{} type] — {}/{} → {}%",
                documentType, score, total, String.format("%.0f", finalScore * 100));

        return finalScore;
    }

    // ══════════════════════════════════════════════════════════════════════════
    // DOCUMENT TYPE DETECTION
    // ══════════════════════════════════════════════════════════════════════════
    private String detectDocumentType(String content, String fileName) {

        // Medical / Hospital
        if (content.contains("patient") || content.contains("diagnosis") ||
            content.contains("prescription") || content.contains("hospital") ||
            content.contains("doctor") || content.contains("dr.") ||
            fileName.contains("medical") || fileName.contains("hospital") ||
            fileName.contains("prescription")) return "MEDICAL";

        // Student / Academic
        if (content.contains("student") || content.contains("register number") ||
            content.contains("roll number") || content.contains("college") ||
            content.contains("university") || content.contains("cgpa") ||
            content.contains("marks") || content.contains("transcript") ||
            fileName.contains("student") || fileName.contains("academic") ||
            fileName.contains("certificate") || fileName.contains("marksheet")) return "STUDENT";

        // HR / Employee
        if (content.contains("employee") || content.contains("designation") ||
            content.contains("salary slip") || content.contains("payslip") ||
            content.contains("offer letter") || content.contains("joining") ||
            content.contains("ctc") || content.contains("human resource") ||
            fileName.contains("hr") || fileName.contains("employee") ||
            fileName.contains("payslip") || fileName.contains("offer")) return "HR";

        // Legal
        if (content.contains("plaintiff") || content.contains("defendant") ||
            content.contains("court") || content.contains("tribunal") ||
            content.contains("advocate") || content.contains("petition") ||
            content.contains("hereby") || content.contains("whereas") ||
            fileName.contains("legal") || fileName.contains("court") ||
            fileName.contains("affidavit")) return "LEGAL";

        // Invoice
        if (content.contains("invoice") || content.contains("bill to") ||
            content.contains("gst") || fileName.contains("invoice")) return "INVOICE";

        // Loan
        if (content.contains("loan") || content.contains("loan application") ||
            fileName.contains("loan")) return "LOAN";

        // Complaint
        if (content.contains("complaint") || content.contains("grievance") ||
            fileName.contains("complaint")) return "COMPLAINT";

        // Contract
        if (content.contains("contract") || content.contains("agreement") ||
            fileName.contains("contract")) return "CONTRACT";

        // Report
        if (content.contains("report") || fileName.contains("report")) return "REPORT";

        // Identity
        if (content.contains("aadhaar") || content.contains("passport") ||
            content.contains("identity") || fileName.contains("id")) return "IDENTITY";

        // Mortgage
        if (content.contains("mortgage") || fileName.contains("mortgage")) return "MORTGAGE";

        // Financial
        if (content.contains("financial") || content.contains("balance sheet") ||
            fileName.contains("financial")) return "FINANCIAL";

        // Application
        if (content.contains("application") || fileName.contains("application")) return "APPLICATION";

        return "GENERAL";
    }
}
