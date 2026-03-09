package com.project.documentworkflow.repository;

import com.project.documentworkflow.model.Document;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentRepository extends JpaRepository<Document, Long> {
}
