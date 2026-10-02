package com.e2edocs.service.validation;

import com.e2edocs.exception.BadRequestException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Service
public class FileValidatorService {

    private final long maxFileSizeBytes;
    private final Set<String> allowedExtensions;
    private final FileSignatureDetector signatureDetector;

    public FileValidatorService(
            @Value("${e2edocs.upload.max-file-size:52428800}") long maxFileSizeBytes, // 50 MB
            @Value("${e2edocs.upload.allowed-extensions:.pdf,.docx,.doc,.txt,.csv,.json,.png,.jpg,.jpeg,.tiff,.tif,.bmp}") String allowedExtStr,
            FileSignatureDetector signatureDetector) {
        this.maxFileSizeBytes = maxFileSizeBytes;
        this.allowedExtensions = new HashSet<>(
                Arrays.stream(allowedExtStr.split(","))
                        .map(String::trim)
                        .map(String::toLowerCase)
                        .toList()
        );
        this.signatureDetector = signatureDetector;
    }

    public void validate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Uploaded file cannot be empty");
        }

        if (file.getSize() > maxFileSizeBytes) {
            long maxMb = maxFileSizeBytes / (1024 * 1024);
            throw new BadRequestException("File size exceeds maximum allowed limit of " + maxMb + "MB");
        }

        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename() != null ? file.getOriginalFilename() : "");
        validateFilename(originalFilename);

        String extension = extractExtension(originalFilename);
        if (!allowedExtensions.contains(extension)) {
            throw new BadRequestException("Unsupported file extension: " + extension + ". Allowed: " + String.join(", ", allowedExtensions));
        }

        try (InputStream is = new BufferedInputStream(file.getInputStream())) {
            if (!signatureDetector.isValidSignatureForExtension(is, extension)) {
                throw new BadRequestException("File content signature does not match extension '" + extension + "'. Disguised or corrupt file.");
            }
        } catch (IOException e) {
            throw new BadRequestException("Failed to read file stream for validation: " + e.getMessage());
        }
    }

    public void validateFilename(String filename) {
        if (filename == null || filename.isBlank()) {
            throw new BadRequestException("Filename cannot be empty");
        }
        if (filename.contains("..") || filename.contains("/") || filename.contains("\\")) {
            throw new BadRequestException("Filename contains invalid path characters: " + filename);
        }
        if (filename.length() > 255) {
            throw new BadRequestException("Filename length exceeds 255 characters");
        }
    }

    public String extractExtension(String filename) {
        if (filename == null) return "";
        int dot = filename.lastIndexOf('.');
        return (dot >= 0) ? filename.substring(dot).toLowerCase() : "";
    }

    public String detectContentType(InputStream is, String fallbackContentType) {
        return signatureDetector.detectMimeType(is, fallbackContentType);
    }
}
