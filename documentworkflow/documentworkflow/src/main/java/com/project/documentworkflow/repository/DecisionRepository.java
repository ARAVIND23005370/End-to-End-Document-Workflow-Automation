package com.project.documentworkflow.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.project.documentworkflow.model.Decision;

public interface DecisionRepository extends JpaRepository<Decision, Long> {
}
