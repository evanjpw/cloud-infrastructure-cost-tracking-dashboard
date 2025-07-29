package com.dashboard.cloud_cost_dashboard.controller;

import com.dashboard.service.interfaces.ScenarioService;
import com.dashboard.dto.scenario.CreateScenarioRequest;
import com.dashboard.dto.scenario.ScenarioComparisonRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/scenarios")
@CrossOrigin(origins = "http://localhost:3000")
public class ScenarioController {

    @Autowired
    private ScenarioService scenarioService;

    @PostMapping
    public Object createScenario(@RequestBody CreateScenarioRequest request) {
        System.out.println("Creating what-if scenario: " + request.getName());
        return scenarioService.createScenario(request);
    }

    @PostMapping("/compare")
    public Object compareScenarios(@RequestBody ScenarioComparisonRequest request) {
        System.out.println("Comparing " + request.getScenarioIds().size() + " scenarios");
        return scenarioService.compareScenarios(request);
    }

    @GetMapping("/templates")
    public Object getScenarioTemplates(@RequestParam(required = false) String difficulty) {
        System.out.println("Fetching scenario templates for difficulty: " + difficulty);
        return scenarioService.getScenarioTemplates(difficulty);
    }

    @PostMapping("/validate")
    public Object validateScenario(@RequestBody CreateScenarioRequest request) {
        System.out.println("Validating scenario parameters");
        return scenarioService.validateScenario(request);
    }
}