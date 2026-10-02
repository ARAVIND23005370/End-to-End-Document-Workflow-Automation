package com.e2edocs.service.extraction;

import java.io.InputStream;

public interface DocumentTextExtractor {
    boolean supports(String mimeType, String fileExtension);
    DocumentExtractionResult extract(InputStream stream, String filename, String mimeType);
    int getPriorityOrder();
}
