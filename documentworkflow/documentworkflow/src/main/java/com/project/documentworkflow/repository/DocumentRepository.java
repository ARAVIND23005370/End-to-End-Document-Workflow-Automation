package com.project.documentworkflow.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.project.documentworkflow.model.Document;

public interface DocumentRepository extends JpaRepository<Document, Long> {
}
