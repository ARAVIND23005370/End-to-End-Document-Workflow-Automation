package com.e2edocs.repository;

import com.e2edocs.entity.DocumentCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocumentCategoryRepository extends JpaRepository<DocumentCategory, String> {
    List<DocumentCategory> findByOrganizationId(String organizationId);
}
