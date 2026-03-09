package com.project.documentworkflow.service;

import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.List;

@Service
public class RealOCRService {

    @Autowired
    private PdfToImageService pdfToImageService;

    public String extractText(File file) {

        ITesseract tesseract = new Tesseract();
        tesseract.setDatapath("C:\\Program Files\\Tesseract-OCR\\tessdata");
        tesseract.setLanguage("eng");

        try {
            if (file.getName().toLowerCase().endsWith(".pdf")) {

                StringBuilder fullText = new StringBuilder();
                List<File> images = pdfToImageService.convertPdfToImages(file);

                for (File image : images) {
                    fullText.append(tesseract.doOCR(image)).append("\n");
                }

                return fullText.toString();

            } else {
                return tesseract.doOCR(file);
            }

        } catch (TesseractException e) {
            throw new RuntimeException("OCR failed", e);
        }
    }
}
