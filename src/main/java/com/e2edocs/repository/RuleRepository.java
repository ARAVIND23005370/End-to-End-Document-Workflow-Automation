package com.e2edocs.repository;

import com.e2edocs.entity.Rule;
import com.e2edocs.entity.enums.RuleStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RuleRepository extends JpaRepository<Rule, String> {
    List<Rule> findByOrganizationIdOrderByEvaluationOrderAsc(String organizationId);
    List<Rule> findByOrganizationIdAndStatusOrderByEvaluationOrderAsc(String organizationId, RuleStatus status);
    long countByOrganizationIdAndStatus(String organizationId, RuleStatus status);
}
