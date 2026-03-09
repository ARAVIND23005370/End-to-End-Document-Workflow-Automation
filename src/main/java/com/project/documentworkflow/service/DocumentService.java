package com.project.documentworkflow.service;

import com.project.documentworkflow.model.Document;
import com.project.documentworkflow.repository.DocumentRepository;
import org.springframework.stereotype.Service;

@Service
public class DocumentService {

    private final DocumentRepository documentRepository;

    public DocumentService(DocumentRepository documentRepository) {
        this.documentRepository = documentRepository;
    }

    public Document saveDocument(Document document) {
        return documentRepository.save(document);
    }

    public Document getDocumentById(Long id) {
        return documentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Document not found"));
    }
}
