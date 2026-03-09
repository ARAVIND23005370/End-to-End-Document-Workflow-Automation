package com.project.documentworkflow.controller;

import com.project.documentworkflow.dto.ApiResponse;
import com.project.documentworkflow.model.OCRData;
import com.project.documentworkflow.repository.OCRDataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ocr-data")
public class OCRDataController {

    @Autowired
    private OCRDataRepository ocrDataRepository;

    @GetMapping
    public ApiResponse<List<OCRData>> getAllOCR() {
        return new ApiResponse<>(true, ocrDataRepository.findAll(), null);
    }

    @GetMapping("/document/{documentId}")
    public ApiResponse<OCRData> getByDocumentId(@PathVariable Long documentId) {
        OCRData ocr = ocrDataRepository.findByDocument_DocumentId(documentId)
                .stream().findFirst().orElse(null);
        if (ocr == null) {
            return new ApiResponse<>(false, null, "No OCR data found for document " + documentId);
        }
        return new ApiResponse<>(true, ocr, null);
    }
}