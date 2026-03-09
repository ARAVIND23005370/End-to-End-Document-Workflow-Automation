package com.project.documentworkflow.repository;

import com.project.documentworkflow.model.Decision;
import com.project.documentworkflow.model.Document;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DecisionRepository extends JpaRepository<Decision, Long> {

    List<Decision> findByDocument(Document document);

}
