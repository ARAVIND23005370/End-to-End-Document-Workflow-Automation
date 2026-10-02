package com.e2edocs.repository;

import com.e2edocs.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, String> {
    List<Department> findByOrganizationId(String organizationId);
    Optional<Department> findByNameIgnoreCase(String name);
}
