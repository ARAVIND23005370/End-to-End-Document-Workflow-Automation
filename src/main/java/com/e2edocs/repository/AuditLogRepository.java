package com.e2edocs.repository;

import com.e2edocs.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, String>, JpaSpecificationExecutor<AuditLog> {
    List<AuditLog> findByOrganizationId(String organizationId);
    List<AuditLog> findTop5ByOrganizationIdOrderByTimestampDesc(String organizationId);
    List<AuditLog> findByResourceIdOrderByTimestampDesc(String resourceId);
}
