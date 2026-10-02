package com.e2edocs.service.extraction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Comparator;
import java.util.List;

@Service
public class DocumentExtractionService {

    private static final Logger logger = LoggerFactory.getLogger(DocumentExtractionService.class);

    private final List<DocumentTextExtractor> extractors;
    private final PlainTextExtractor fallbackExtractor;

    public DocumentExtractionService(List<DocumentTextExtractor> extractors, PlainTextExtractor fallbackExtractor) {
        this.extractors = extractors.stream()
                .sorted(Comparator.comparingInt(DocumentTextExtractor::getPriorityOrder))
                .toList();
        this.fallbackExtractor = fallbackExtractor;
    }

    public DocumentExtractionResult extract(InputStream stream, String filename, String mimeType) {
        String extension = extractExtension(filename);

        try {
            byte[] fileBytes = stream.readAllBytes();

            for (DocumentTextExtractor extractor : extractors) {
                if (extractor.supports(mimeType, extension)) {
                    logger.debug("Selected extractor '{}' for file '{}' (mime: {}, ext: {})",
                            extractor.getClass().getSimpleName(), filename, mimeType, extension);
                    return extractor.extract(new ByteArrayInputStream(fileBytes), filename, mimeType);
                }
            }

            logger.warn("No dedicated extractor matched for file '{}' (mime: {}, ext: {}). Using plain text fallback.",
                    filename, mimeType, extension);
            return fallbackExtractor.extract(new ByteArrayInputStream(fileBytes), filename, mimeType);
        } catch (Exception e) {
            logger.error("Failed to extract content from document {}: {}", filename, e.getMessage());
            return new DocumentExtractionResult(
                    "[Content Extraction Failed: " + e.getMessage() + "]",
                    "Fallback",
                    false,
                    0
            );
        }
    }

    private String extractExtension(String filename) {
        if (filename == null) return "";
        int dot = filename.lastIndexOf('.');
        return (dot >= 0) ? filename.substring(dot).toLowerCase() : "";
    }
}
