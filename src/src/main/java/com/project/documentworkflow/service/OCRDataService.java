package com.project.documentworkflow.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.project.documentworkflow.model.OCRData;
import com.project.documentworkflow.repository.OCRDataRepository;

@Service
public class OCRDataService {

    @Autowired
    private OCRDataRepository ocrDataRepository;

    public OCRData saveOCRData(OCRData ocrData) {
        return ocrDataRepository.save(ocrData);
    }
}
