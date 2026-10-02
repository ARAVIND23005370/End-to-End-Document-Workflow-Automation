package com.e2edocs;

import com.e2edocs.service.extraction.*;
import com.e2edocs.service.ocr.OcrService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class DocumentExtractionServiceTest {

    private DocumentExtractionService extractionService;
    private OcrService ocrService;

    @BeforeEach
    void setUp() {
        ocrService = Mockito.mock(OcrService.class);
        when(ocrService.isAvailable()).thenReturn(false);

        PdfTextExtractor pdfExtractor = new PdfTextExtractor(ocrService);
        DocxTextExtractor docxExtractor = new DocxTextExtractor();
        PlainTextExtractor plainTextExtractor = new PlainTextExtractor();
        ImageOcrExtractor imageExtractor = new ImageOcrExtractor(ocrService);

        extractionService = new DocumentExtractionService(
                List.of(pdfExtractor, docxExtractor, plainTextExtractor, imageExtractor),
                plainTextExtractor
        );
    }

    @Test
    @DisplayName("Extract text from plain text document")
    void testPlainTextExtraction() {
        String content = "Invoice Number: INV-2026-991\nTotal Amount: $14,500.00\nVendor: Acme Corp";
        ByteArrayInputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));

        DocumentExtractionResult result = extractionService.extract(is, "invoice.txt", "text/plain");

        assertNotNull(result);
        assertTrue(result.getExtractedText().contains("INV-2026-991"));
        assertTrue(result.getExtractedText().contains("14,500.00"));
        assertNotNull(result.getContentPreview());
        assertEquals("Plain Text Reader", result.getExtractorUsed());
    }

    @Test
    @DisplayName("Extract text from PDF document content stream")
    void testPdfTextExtraction() {
        String pdfContent = "%PDF-1.4\n1 0 obj\n<< /Length 50 >>\nstream\nBT\n/F1 12 Tf\n(E2EDocs Confidential Agreement 2026 - Approved by Legal) Tj\nET\nendstream\nendobj\n";
        ByteArrayInputStream is = new ByteArrayInputStream(pdfContent.getBytes(StandardCharsets.ISO_8859_1));

        DocumentExtractionResult result = extractionService.extract(is, "agreement.pdf", "application/pdf");

        assertNotNull(result);
        assertTrue(result.getExtractedText().contains("E2EDocs Confidential Agreement 2026"));
        assertEquals(1, result.getPageCount());
        assertFalse(result.isOcrUsed());
        assertEquals("PDF Native Stream Extractor", result.getExtractorUsed());
    }

    @Test
    @DisplayName("Extract text from DOCX OpenXML document archive")
    void testDocxTextExtraction() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(out)) {
            ZipEntry entry = new ZipEntry("word/document.xml");
            zos.putNextEntry(entry);
            String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
                    "<w:document xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\">\n" +
                    "  <w:body>\n" +
                    "    <w:p><w:r><w:t>Monthly Financial Statement - Department: Finance - Priority: Critical</w:t></w:r></w:p>\n" +
                    "  </w:body>\n" +
                    "</w:document>";
            zos.write(xml.getBytes(StandardCharsets.UTF_8));
            zos.closeEntry();
        }

        ByteArrayInputStream is = new ByteArrayInputStream(out.toByteArray());
        DocumentExtractionResult result = extractionService.extract(
                is, "financial_report.docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document");

        assertNotNull(result);
        assertTrue(result.getExtractedText().contains("Monthly Financial Statement"));
        assertTrue(result.getExtractedText().contains("Department: Finance"));
        assertEquals("OpenXML DOCX Extractor", result.getExtractorUsed());
    }

    @Test
    @DisplayName("Fallback extractor gracefully handles unsupported or unknown file formats")
    void testFallbackExtraction() {
        String data = "key1=value1\nkey2=value2";
        ByteArrayInputStream is = new ByteArrayInputStream(data.getBytes(StandardCharsets.UTF_8));

        DocumentExtractionResult result = extractionService.extract(is, "custom.conf", "application/octet-stream");

        assertNotNull(result);
        assertTrue(result.getExtractedText().contains("key1=value1"));
    }
}
