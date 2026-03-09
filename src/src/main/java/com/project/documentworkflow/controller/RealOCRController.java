package com.project.documentworkflow.controller;

import com.project.documentworkflow.model.OCRData;
import com.project.documentworkflow.service.OCRDataService;
import com.project.documentworkflow.service.RealOCRService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

@RestController
@RequestMapping("/api/real-ocr")
public class RealOCRController {

    @Autowired
    private RealOCRService realOCRService;

    @Autowired
    private OCRDataService ocrDataService;

    @PostMapping("/extract")
    public OCRData extractAndSaveOCR(@RequestParam("file") MultipartFile file) throws IOException {

        File tempFile = File.createTempFile("upload-", file.getOriginalFilename());
        file.transferTo(tempFile);

        // 1️⃣ Extract text using OCR
        String extractedText = realOCRService.extractText(tempFile);

        // 2️⃣ Create OCRData entity
        OCRData ocrData = new OCRData();
        ocrData.setExtractedText(extractedText);

        // 3️⃣ Dummy confidence score (acceptable for now)
        if (extractedText.length() > 50) {
            ocrData.setConfidenceScore(0.85);
        } else {
            ocrData.setConfidenceScore(0.60);
        }

        // 4️⃣ Save to DB
        return ocrDataService.saveOCRData(ocrData);
    }
}
