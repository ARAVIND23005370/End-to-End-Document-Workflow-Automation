package com.e2edocs.service.ocr;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class TesseractOcrService implements OcrService {

    private static final Logger logger = LoggerFactory.getLogger(TesseractOcrService.class);

    private final String tesseractPath;
    private final String language;
    private final boolean ocrEnabled;
    private Boolean isTesseractAvailable = null;

    public TesseractOcrService(
            @Value("${e2edocs.ocr.tesseract.path:tesseract}") String tesseractPath,
            @Value("${e2edocs.ocr.language:eng}") String language,
            @Value("${e2edocs.ocr.enabled:true}") boolean ocrEnabled) {
        this.tesseractPath = tesseractPath;
        this.language = language;
        this.ocrEnabled = ocrEnabled;
    }

    @Override
    public boolean isAvailable() {
        if (!ocrEnabled) return false;
        if (isTesseractAvailable != null) {
            return isTesseractAvailable;
        }

        try {
            ProcessBuilder pb = new ProcessBuilder(tesseractPath, "--version");
            pb.redirectErrorStream(true);
            Process process = pb.start();
            boolean completed = process.waitFor(3, TimeUnit.SECONDS);
            isTesseractAvailable = completed && process.exitValue() == 0;
            if (isTesseractAvailable) {
                logger.info("Tesseract OCR engine successfully detected in system path: {}", tesseractPath);
            } else {
                logger.warn("Tesseract OCR binary not found or timed out. OCR will run in fallback mode.");
            }
        } catch (Exception e) {
            isTesseractAvailable = false;
            logger.info("Tesseract OCR not installed locally ({}: {}). OCR will run in fallback mode.", e.getClass().getSimpleName(), e.getMessage());
        }

        return isTesseractAvailable;
    }

    @Override
    public String extractText(InputStream imageStream, String format) {
        if (imageStream == null) return "";
        try {
            BufferedImage image = ImageIO.read(imageStream);
            if (image == null) {
                logger.warn("Failed to decode image stream into BufferedImage for format: {}", format);
                return "";
            }
            return extractText(image);
        } catch (IOException e) {
            logger.error("Error reading image stream for OCR: {}", e.getMessage());
            return "";
        }
    }

    @Override
    public String extractText(BufferedImage image) {
        if (image == null) return "";

        if (!isAvailable()) {
            logger.debug("Tesseract OCR is not available on host. Returning structured image metadata marker.");
            return "[Image Content: " + image.getWidth() + "x" + image.getHeight() + " px, OCR engine not available]";
        }

        Path tempImage = null;
        Path tempOutputBase = null;

        try {
            tempImage = Files.createTempFile("ocr_in_" + UUID.randomUUID(), ".png");
            tempOutputBase = Files.createTempFile("ocr_out_" + UUID.randomUUID(), "");
            Files.deleteIfExists(tempOutputBase); // Tesseract appends .txt automatically

            ImageIO.write(image, "png", tempImage.toFile());

            ProcessBuilder pb = new ProcessBuilder(
                    tesseractPath,
                    tempImage.toAbsolutePath().toString(),
                    tempOutputBase.toAbsolutePath().toString(),
                    "-l", language
            );
            pb.redirectErrorStream(true);
            Process process = pb.start();

            boolean finished = process.waitFor(30, TimeUnit.SECONDS);
            if (!finished) {
                process.destroyForcibly();
                logger.warn("Tesseract OCR process timed out after 30 seconds");
                return "";
            }

            Path outputFile = Path.of(tempOutputBase.toAbsolutePath() + ".txt");
            if (Files.exists(outputFile)) {
                String text = Files.readString(outputFile).trim();
                Files.deleteIfExists(outputFile);
                return text;
            }

            return "";
        } catch (Exception e) {
            logger.error("Error executing Tesseract OCR: {}", e.getMessage());
            return "";
        } finally {
            if (tempImage != null) {
                try {
                    Files.deleteIfExists(tempImage);
                } catch (IOException ignored) {
                }
            }
        }
    }
}
