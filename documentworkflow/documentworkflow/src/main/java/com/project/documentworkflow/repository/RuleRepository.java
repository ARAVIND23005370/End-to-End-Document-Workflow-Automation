package com.project.documentworkflow.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.project.documentworkflow.model.Rule;

public interface RuleRepository extends JpaRepository<Rule, Long> {
}
