package com.dashboard.cloud_cost_dashboard.controller;

import com.dashboard.service.impl.ScenarioDataService;
import com.dashboard.templates.ScenarioTemplates;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/scenario-testing")
@CrossOrigin(origins = "*")
public class ScenarioTestingController {
    
    @Autowired
    private ScenarioDataService scenarioDataService;
    
    /**
     * Get all available scenario templates
     */
    @GetMapping("/templates")
    public ResponseEntity<Map<String, Object>> getAvailableTemplates(
            @RequestParam(required = false) String difficulty,
            @RequestParam(required = false) String type) {
        
        List<Map<String, Object>> templates;
        
        if (difficulty != null) {
            templates = ScenarioTemplates.getTemplatesByDifficulty(difficulty);
        } else if (type != null) {
            templates = ScenarioTemplates.getTemplatesByType(type);
        } else {
            templates = ScenarioTemplates.getAllTemplates();
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("templates", templates);
        response.put("total", templates.size());
        response.put("difficulties", List.of("beginner", "intermediate", "advanced"));
        response.put("types", List.of("rightsizing", "cost_optimization", "reserved_instances", 
                                     "spot_instances", "infrastructure_change", "scaling"));
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Generate a new scenario session for testing
     */
    @PostMapping("/generate")
    public ResponseEntity<Map<String, Object>> generateScenario(@RequestBody GenerateScenarioRequest request) {
        try {
            // Validate template exists
            Map<String, Object> template = ScenarioTemplates.getTemplateById(request.getTemplateId());
            if (template == null) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Invalid template ID: " + request.getTemplateId()));
            }
            
            // Generate scenario
            String sessionId = scenarioDataService.generateScenarioSession(
                request.getTemplateId(), 
                request.getStudentIdentifier()
            );
            
            Map<String, Object> response = new HashMap<>();
            response.put("sessionId", sessionId);
            response.put("template", template);
            response.put("studentIdentifier", request.getStudentIdentifier());
            response.put("dashboardUrl", "/dashboard?scenario=" + sessionId);
            response.put("message", "Scenario generated successfully. Student can now access the dashboard.");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Failed to generate scenario: " + e.getMessage()));
        }
    }
    
    /**
     * Get scenario session details (for student view)
     */
    @GetMapping("/session/{sessionId}")
    public ResponseEntity<Map<String, Object>> getScenarioSession(@PathVariable String sessionId) {
        // This would return the scenario data that students can see
        // Implementation would query scenario_sessions and scenario_usage_data tables
        Map<String, Object> response = new HashMap<>();
        response.put("sessionId", sessionId);
        response.put("message", "Load this data in the dashboard for analysis");
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get grading information (for instructor view)
     */
    @GetMapping("/grading/{sessionId}")
    public ResponseEntity<Map<String, Object>> getGradingInfo(@PathVariable String sessionId) {
        // This would return grading keys and optimization hints
        // Only accessible to instructors/graders
        Map<String, Object> response = new HashMap<>();
        response.put("sessionId", sessionId);
        response.put("message", "Grading keys would be returned here");
        return ResponseEntity.ok(response);
    }
    
    /**
     * Submit student recommendations for grading
     */
    @PostMapping("/submit/{sessionId}")
    public ResponseEntity<Map<String, Object>> submitRecommendations(
            @PathVariable String sessionId,
            @RequestBody StudentSubmission submission) {
        // Store student submission for grading
        Map<String, Object> response = new HashMap<>();
        response.put("sessionId", sessionId);
        response.put("submitted", true);
        response.put("message", "Recommendations submitted successfully");
        return ResponseEntity.ok(response);
    }
    
    // Request/Response DTOs
    
    static class GenerateScenarioRequest {
        private String templateId;
        private String studentIdentifier;
        
        public String getTemplateId() { return templateId; }
        public void setTemplateId(String templateId) { this.templateId = templateId; }
        public String getStudentIdentifier() { return studentIdentifier; }
        public void setStudentIdentifier(String studentIdentifier) { this.studentIdentifier = studentIdentifier; }
    }
    
    static class StudentSubmission {
        private String studentIdentifier;
        private List<Recommendation> recommendations;
        
        public String getStudentIdentifier() { return studentIdentifier; }
        public void setStudentIdentifier(String studentIdentifier) { this.studentIdentifier = studentIdentifier; }
        public List<Recommendation> getRecommendations() { return recommendations; }
        public void setRecommendations(List<Recommendation> recommendations) { this.recommendations = recommendations; }
    }
    
    static class Recommendation {
        private String optimizationType;
        private String targetResource;
        private String description;
        private Double estimatedSavingsPercent;
        private String implementationPlan;
        
        // Getters and setters
        public String getOptimizationType() { return optimizationType; }
        public void setOptimizationType(String optimizationType) { this.optimizationType = optimizationType; }
        public String getTargetResource() { return targetResource; }
        public void setTargetResource(String targetResource) { this.targetResource = targetResource; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public Double getEstimatedSavingsPercent() { return estimatedSavingsPercent; }
        public void setEstimatedSavingsPercent(Double estimatedSavingsPercent) { this.estimatedSavingsPercent = estimatedSavingsPercent; }
        public String getImplementationPlan() { return implementationPlan; }
        public void setImplementationPlan(String implementationPlan) { this.implementationPlan = implementationPlan; }
    }
}