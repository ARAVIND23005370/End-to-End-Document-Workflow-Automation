package com.project.documentworkflow.service;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Service
public class PdfToImageService {

    public List<File> convertPdfToImages(File pdfFile) {
        List<File> imageFiles = new ArrayList<>();

        try (PDDocument document = PDDocument.load(pdfFile)) {

            PDFRenderer pdfRenderer = new PDFRenderer(document);

            for (int page = 0; page < document.getNumberOfPages(); page++) {
                BufferedImage image = pdfRenderer.renderImageWithDPI(page, 300);

                File imageFile = File.createTempFile(
                        "pdf-page-" + page, ".png"
                );

                ImageIO.write(image, "png", imageFile);
                imageFiles.add(imageFile);
            }

        } catch (Exception e) {
            throw new RuntimeException("Failed to convert PDF to images", e);
        }

        return imageFiles;
    }
}
