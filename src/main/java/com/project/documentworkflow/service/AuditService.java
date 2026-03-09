package com.project.documentworkflow.service;

import com.project.documentworkflow.model.AuditLog;
import com.project.documentworkflow.repository.AuditLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AuditService {

    @Autowired
    private AuditLogRepository auditLogRepository;

    public AuditLog log(Long documentId, String action, String performedBy, String details) {
        AuditLog log = new AuditLog();
        log.setDocumentId(documentId);
        log.setAction(action);
        log.setPerformedBy(performedBy);
        log.setDetails(details);
        return auditLogRepository.save(log);
    }

    public List<AuditLog> getLogsForDocument(Long documentId) {
        return auditLogRepository.findByDocumentIdOrderByActionTimeDesc(documentId);
    }

    public List<AuditLog> getAllLogs() {
        return auditLogRepository.findAllByOrderByActionTimeDesc();
    }
}
