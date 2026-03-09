package com.project.documentworkflow.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.project.documentworkflow.model.Document;
import com.project.documentworkflow.service.DocumentService;

@RestController
@RequestMapping("/api/documents")
public class DocumentController {

    @Autowired
    private DocumentService documentService;

    @PostMapping
    public Document createDocument(@RequestBody Document document) {
        return documentService.saveDocument(document);
    }
}
