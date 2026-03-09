package com.project.documentworkflow.controller;

import com.project.documentworkflow.dto.ApiResponse;
import com.project.documentworkflow.dto.ForwardRequest;
import com.project.documentworkflow.model.DocumentForward;
import com.project.documentworkflow.security.JwtUtil;
import com.project.documentworkflow.service.DocumentForwardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * DocumentForwardController
 *
 * FEATURE: Send document to any department or person.
 *
 * POST /api/forward             → Forward a document to a department or email
 * GET  /api/forward/{docId}     → View forward history for a document
 * GET  /api/forward             → View all forwards (ADMIN)
 *
 * Available departments: Finance, HR, Legal, IT, Operations, Management
 */
@RestController
@RequestMapping("/api/forward")
public class DocumentForwardController {

    @Autowired
    private DocumentForwardService documentForwardService;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping
    public ApiResponse<DocumentForward> forwardDocument(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody ForwardRequest request) {

        String token = authHeader.replace("Bearer ", "");
        String userEmail = jwtUtil.extractEmail(token);

        DocumentForward forward = documentForwardService.forwardDocument(
            request.getDocumentId(),
            userEmail,
            request.getForwardedTo(),
            request.getForwardType(),
            request.getNote()
        );

        return new ApiResponse<>(true, forward, null);
    }

    @GetMapping("/{documentId}")
    public ApiResponse<List<DocumentForward>> getForwardsForDocument(@PathVariable Long documentId) {
        return new ApiResponse<>(true, documentForwardService.getForwardsForDocument(documentId), null);
    }

    @GetMapping
    public ApiResponse<List<DocumentForward>> getAllForwards() {
        return new ApiResponse<>(true, documentForwardService.getAllForwards(), null);
    }
}
