package com.e2edocs;

import com.e2edocs.exception.BadRequestException;
import com.e2edocs.service.validation.FileSignatureDetector;
import com.e2edocs.service.validation.FileValidatorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class FileValidatorServiceTest {

    private FileValidatorService validator;

    @BeforeEach
    void setUp() {
        FileSignatureDetector detector = new FileSignatureDetector();
        validator = new FileValidatorService(
                10 * 1024 * 1024, // 10 MB limit
                ".pdf,.docx,.txt,.png,.jpg,.jpeg,.json,.csv",
                detector
        );
    }

    @Test
    @DisplayName("Valid text file passes validation")
    void testValidTextFile() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "report.txt", "text/plain", "Hello World Content".getBytes(StandardCharsets.UTF_8));

        assertDoesNotThrow(() -> validator.validate(file));
    }

    @Test
    @DisplayName("Empty file is rejected")
    void testEmptyFileRejected() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "empty.txt", "text/plain", new byte[0]);

        assertThrows(BadRequestException.class, () -> validator.validate(file));
    }

    @Test
    @DisplayName("Unsupported file extension is rejected")
    void testUnsupportedExtensionRejected() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "script.exe", "application/x-msdownload", "binary content".getBytes(StandardCharsets.UTF_8));

        assertThrows(BadRequestException.class, () -> validator.validate(file));
    }

    @Test
    @DisplayName("File exceeding size limit is rejected")
    void testFileSizeExceeded() {
        byte[] largeBytes = new byte[11 * 1024 * 1024]; // 11 MB > 10 MB
        MockMultipartFile file = new MockMultipartFile(
                "file", "large.txt", "text/plain", largeBytes);

        assertThrows(BadRequestException.class, () -> validator.validate(file));
    }

    @Test
    @DisplayName("Path traversal in filename is rejected")
    void testPathTraversalRejected() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "../../etc/passwd.txt", "text/plain", "secret".getBytes(StandardCharsets.UTF_8));

        assertThrows(BadRequestException.class, () -> validator.validate(file));
    }

    @Test
    @DisplayName("Disguised binary file pretending to be PDF is rejected by signature detector")
    void testDisguisedPdfRejected() {
        byte[] fakePdfBytes = "THIS_IS_NOT_A_VALID_PDF_HEADER".getBytes(StandardCharsets.UTF_8);
        MockMultipartFile file = new MockMultipartFile(
                "file", "fake.pdf", "application/pdf", fakePdfBytes);

        assertThrows(BadRequestException.class, () -> validator.validate(file));
    }

    @Test
    @DisplayName("Valid PDF header signature passes validation")
    void testValidPdfHeaderAccepted() {
        byte[] validPdfBytes = "%PDF-1.7\nSample content".getBytes(StandardCharsets.UTF_8);
        MockMultipartFile file = new MockMultipartFile(
                "file", "legit.pdf", "application/pdf", validPdfBytes);

        assertDoesNotThrow(() -> validator.validate(file));
    }
}
