package com.project.documentworkflow.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.project.documentworkflow.model.OCRData;

public interface OCRDataRepository extends JpaRepository<OCRData, Long> {
}
