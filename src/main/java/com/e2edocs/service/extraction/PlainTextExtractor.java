package com.e2edocs.service.extraction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Set;

@Component
public class PlainTextExtractor implements DocumentTextExtractor {

    private static final Logger logger = LoggerFactory.getLogger(PlainTextExtractor.class);
    private static final Set<String> SUPPORTED_EXTS = Set.of(".txt", ".csv", ".json", ".log", ".xml", ".md", ".html");

    @Override
    public boolean supports(String mimeType, String fileExtension) {
        if (mimeType != null && (mimeType.startsWith("text/") || mimeType.equals("application/json") || mimeType.equals("application/xml"))) {
            return true;
        }
        if (fileExtension != null && SUPPORTED_EXTS.contains(fileExtension.toLowerCase())) {
            return true;
        }
        return false;
    }

    @Override
    public DocumentExtractionResult extract(InputStream stream, String filename, String mimeType) {
        try {
            byte[] bytes = stream.readAllBytes();
            String text;
            try {
                text = new String(bytes, StandardCharsets.UTF_8);
            } catch (Exception e) {
                text = new String(bytes, StandardCharsets.ISO_8859_1);
            }

            int lineCount = text.split("\r\n|\r|\n").length;
            DocumentExtractionResult result = new DocumentExtractionResult(text, "Plain Text Reader", false, 1);
            result.getMetadata().put("text.lineCount", lineCount);
            return result;
        } catch (Exception e) {
            logger.error("Error reading plain text file {}: {}", filename, e.getMessage());
            return new DocumentExtractionResult(
                    "[Text Read Error: " + e.getMessage() + "]",
                    "Plain Text (Failed)",
                    false,
                    0
            );
        }
    }

    @Override
    public int getPriorityOrder() {
        return 30;
    }
}
