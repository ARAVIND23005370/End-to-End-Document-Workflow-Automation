package com.project.documentworkflow.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.project.documentworkflow.model.Decision;
import com.project.documentworkflow.service.DecisionService;

@RestController
@RequestMapping("/api/decisions")
public class DecisionController {

    @Autowired
    private DecisionService decisionService;

    @PostMapping
    public Decision createDecision(@RequestBody Decision decision) {
        return decisionService.saveDecision(decision);
    }
}
