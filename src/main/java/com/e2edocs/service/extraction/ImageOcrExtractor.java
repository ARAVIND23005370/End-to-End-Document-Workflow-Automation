package com.e2edocs.service.extraction;

import com.e2edocs.service.ocr.OcrService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Set;

@Component
public class ImageOcrExtractor implements DocumentTextExtractor {

    private static final Logger logger = LoggerFactory.getLogger(ImageOcrExtractor.class);
    private static final Set<String> SUPPORTED_EXTS = Set.of(".png", ".jpg", ".jpeg", ".tiff", ".tif", ".bmp", ".gif");

    private final OcrService ocrService;

    public ImageOcrExtractor(OcrService ocrService) {
        this.ocrService = ocrService;
    }

    @Override
    public boolean supports(String mimeType, String fileExtension) {
        if (mimeType != null && mimeType.startsWith("image/")) {
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
            byte[] imageBytes = stream.readAllBytes();
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(imageBytes));

            int width = image != null ? image.getWidth() : 0;
            int height = image != null ? image.getHeight() : 0;

            String text = "";
            boolean ocrUsed = false;
            String extractorName = "Image Processing (Metadata only)";

            if (ocrService.isAvailable() && image != null) {
                text = ocrService.extractText(image);
                ocrUsed = true;
                extractorName = "Tesseract OCR (Image)";
            } else if (image != null) {
                text = "[Image Document: " + filename + " (" + width + "x" + height + " px). OCR Engine not active]";
            } else {
                text = "[Image Document: " + filename + "]";
            }

            DocumentExtractionResult result = new DocumentExtractionResult(text, extractorName, ocrUsed, 1);
            result.getMetadata().put("image.width", width);
            result.getMetadata().put("image.height", height);
            result.getMetadata().put("image.format", mimeType);

            return result;
        } catch (Exception e) {
            logger.error("Error performing OCR on image {}: {}", filename, e.getMessage());
            return new DocumentExtractionResult(
                    "[Image OCR Error: " + e.getMessage() + "]",
                    "Image OCR (Failed)",
                    false,
                    0
            );
        }
    }

    @Override
    public int getPriorityOrder() {
        return 40;
    }
}
