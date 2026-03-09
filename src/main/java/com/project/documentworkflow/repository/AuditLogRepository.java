package com.project.documentworkflow.repository;

import com.project.documentworkflow.model.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    List<AuditLog> findByDocumentIdOrderByActionTimeDesc(Long documentId);
    List<AuditLog> findAllByOrderByActionTimeDesc();
}
