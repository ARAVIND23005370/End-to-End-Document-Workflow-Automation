package com.project.documentworkflow.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.project.documentworkflow.model.Decision;
import com.project.documentworkflow.repository.DecisionRepository;

@Service
public class DecisionService {

    @Autowired
    private DecisionRepository decisionRepository;

    public Decision saveDecision(Decision decision) {
        return decisionRepository.save(decision);
    }
}
