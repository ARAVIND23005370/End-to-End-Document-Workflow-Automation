package com.project.documentworkflow.controller;

import com.project.documentworkflow.dto.ApiResponse;
import com.project.documentworkflow.model.Decision;
import com.project.documentworkflow.repository.DecisionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/decisions")
public class DecisionController {

    @Autowired
    private DecisionRepository decisionRepository;

    @GetMapping
    public ApiResponse<List<Decision>> getAllDecisions() {
        return new ApiResponse<>(true, decisionRepository.findAll(), null);
    }

    @GetMapping("/{id}")
    public ApiResponse<Decision> getDecisionById(@PathVariable Long id) {
        Decision decision = decisionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Decision not found"));
        return new ApiResponse<>(true, decision, null);
    }
}