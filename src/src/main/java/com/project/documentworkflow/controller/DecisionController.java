package com.project.documentworkflow.controller;

import com.project.documentworkflow.dto.ApiResponse;
import com.project.documentworkflow.dto.DecisionHistoryResponse;
import com.project.documentworkflow.service.DecisionService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class DecisionController {

    @Autowired
    private DecisionService decisionService;

    @GetMapping("/document/{id}/history")
    public ApiResponse<DecisionHistoryResponse> getHistory(@PathVariable Long id) {

        DecisionHistoryResponse response = decisionService.getHistory(id);

        return new ApiResponse<>(true, response, null);
    }
}
