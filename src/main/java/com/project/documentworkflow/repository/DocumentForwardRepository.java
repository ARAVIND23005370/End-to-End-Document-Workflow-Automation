package com.project.documentworkflow.repository;

import com.project.documentworkflow.model.DocumentForward;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface DocumentForwardRepository extends JpaRepository<DocumentForward, Long> {
    List<DocumentForward> findByDocument_DocumentId(Long documentId);
}
