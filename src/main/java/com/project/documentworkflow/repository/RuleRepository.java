package com.project.documentworkflow.repository;

import com.project.documentworkflow.model.Rule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RuleRepository extends JpaRepository<Rule, Long> {
    // Returns all active rules sorted by priority (1 = highest)
    List<Rule> findAllByOrderByPriorityAsc();
    List<Rule> findByActiveTrueOrderByPriorityAsc();
}
