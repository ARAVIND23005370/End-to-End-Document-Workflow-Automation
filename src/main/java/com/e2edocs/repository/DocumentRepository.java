package com.e2edocs.repository;

import com.e2edocs.entity.Document;
import com.e2edocs.entity.enums.DocumentStatus;
import com.e2edocs.entity.enums.Priority;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocumentRepository extends JpaRepository<Document, String>, JpaSpecificationExecutor<Document> {
    
    long countByOrganizationId(String organizationId);
    long countByOrganizationIdAndStatus(String organizationId, DocumentStatus status);
    long countByOrganizationIdAndPriority(String organizationId, Priority priority);
    
    List<Document> findTop5ByOrganizationIdOrderByCreatedAtDesc(String organizationId);
    
    @Query("SELECT d.status, COUNT(d) FROM Document d WHERE d.organizationId = :organizationId GROUP BY d.status")
    List<Object[]> countByStatusGrouped(String organizationId);

    @Query("SELECT d.department, COUNT(d) FROM Document d WHERE d.organizationId = :organizationId AND d.department IS NOT NULL GROUP BY d.department")
    List<Object[]> countByDepartmentGrouped(String organizationId);
}
