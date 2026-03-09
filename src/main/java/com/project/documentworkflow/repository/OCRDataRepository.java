package com.project.documentworkflow.repository;

import com.project.documentworkflow.model.OCRData;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OCRDataRepository extends JpaRepository<OCRData, Long> {
    List<OCRData> findByDocument_DocumentId(Long documentId);
}