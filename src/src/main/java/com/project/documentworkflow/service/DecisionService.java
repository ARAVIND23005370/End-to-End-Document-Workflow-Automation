package com.project.documentworkflow.service;

import com.project.documentworkflow.dto.DecisionHistoryResponse;
import com.project.documentworkflow.exception.DocumentNotFoundException;
import com.project.documentworkflow.model.Decision;
import com.project.documentworkflow.model.Document;
import com.project.documentworkflow.repository.DecisionRepository;
import com.project.documentworkflow.repository.DocumentRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DecisionService {

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private DecisionRepository decisionRepository;

    public DecisionHistoryResponse getHistory(Long id) {

        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new DocumentNotFoundException("Document not found"));


        List<Decision> decisions = decisionRepository.findByDocument(document);

        DecisionHistoryResponse response = new DecisionHistoryResponse();
        response.setDocumentId(document.getDocumentId());
        response.setStatus(document.getStatus());

        List<String> decisionTypes = decisions.stream()
                .map(Decision::getDecisionType)
                .toList();

        response.setDecisions(decisionTypes);

        return response;
    }

    public static class JwtService {
    }
}
