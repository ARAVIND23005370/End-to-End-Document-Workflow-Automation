package com.project.documentworkflow.repository;

import com.project.documentworkflow.model.OCRData;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OCRDataRepository extends JpaRepository<OCRData, Long> {
}
