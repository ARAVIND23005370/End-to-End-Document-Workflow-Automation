package com.e2edocs.service.ocr;

import java.awt.image.BufferedImage;
import java.io.InputStream;

public interface OcrService {
    boolean isAvailable();
    String extractText(InputStream imageStream, String format);
    String extractText(BufferedImage image);
}
