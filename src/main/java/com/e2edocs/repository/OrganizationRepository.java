package com.e2edocs.repository;

import com.e2edocs.entity.Organization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrganizationRepository extends JpaRepository<Organization, String> {
    Optional<Organization> findByCode(String code);
    boolean existsByCode(String code);
}
