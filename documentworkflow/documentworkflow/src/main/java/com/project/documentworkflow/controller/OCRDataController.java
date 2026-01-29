package com.project.documentworkflow.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.project.documentworkflow.model.OCRData;
import com.project.documentworkflow.service.OCRDataService;

@RestController
@RequestMapping("/api/ocr")
public class OCRDataController {

    @Autowired
    private OCRDataService ocrDataService;

    @PostMapping
    public OCRData createOCRData(@RequestBody OCRData ocrData) {
        return ocrDataService.saveOCRData(ocrData);
    }
}
