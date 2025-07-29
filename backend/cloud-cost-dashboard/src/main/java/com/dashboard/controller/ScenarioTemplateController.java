package com.dashboard.controller;

import com.dashboard.templates.ScenarioTemplates;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/scenario-templates")
@CrossOrigin(origins = "*")
public class ScenarioTemplateController {
    
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllTemplates(
            @RequestParam(required = false) String difficulty,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        
        List<Map<String, Object>> templates;
        
        // Filter by difficulty or get all
        if (difficulty != null && !difficulty.isEmpty()) {
            templates = ScenarioTemplates.getTemplatesByDifficulty(difficulty);
        } else {
            templates = ScenarioTemplates.getAllTemplates();
        }
        
        // Further filter by type if specified
        if (type != null && !type.isEmpty()) {
            String finalType = type;
            templates = templates.stream()
                .filter(t -> finalType.equals(t.get("type")))
                .toList();
        }
        
        // Further filter by category if specified
        if (category != null && !category.isEmpty()) {
            String finalCategory = category;
            templates = templates.stream()
                .filter(t -> finalCategory.equals(t.get("category")))
                .toList();
        }
        
        // Pagination
        int pageNum = page != null ? page : 0;
        int pageSize = size != null ? size : 20;
        int start = pageNum * pageSize;
        int end = Math.min(start + pageSize, templates.size());
        
        List<Map<String, Object>> paginatedTemplates = templates.subList(start, end);
        
        // Build response
        Map<String, Object> response = new HashMap<>();
        response.put("templates", paginatedTemplates);
        response.put("totalTemplates", templates.size());
        response.put("currentPage", pageNum);
        response.put("pageSize", pageSize);
        response.put("totalPages", (int) Math.ceil((double) templates.size() / pageSize));
        
        // Add summary statistics
        Map<String, Object> stats = new HashMap<>();
        stats.put("byDifficulty", getCountByField(templates, "difficulty"));
        stats.put("byType", getCountByField(templates, "type"));
        stats.put("byCategory", getCountByField(templates, "category"));
        response.put("statistics", stats);
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getTemplateById(@PathVariable String id) {
        Map<String, Object> template = ScenarioTemplates.getTemplateById(id);
        if (template != null) {
            return ResponseEntity.ok(template);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    @GetMapping("/categories")
    public ResponseEntity<List<String>> getCategories() {
        Set<String> categories = new HashSet<>();
        ScenarioTemplates.getAllTemplates().forEach(template -> {
            Object category = template.get("category");
            if (category != null) {
                categories.add(category.toString());
            }
        });
        return ResponseEntity.ok(new ArrayList<>(categories));
    }
    
    @GetMapping("/types")
    public ResponseEntity<List<String>> getTypes() {
        Set<String> types = new HashSet<>();
        ScenarioTemplates.getAllTemplates().forEach(template -> {
            Object type = template.get("type");
            if (type != null) {
                types.add(type.toString());
            }
        });
        return ResponseEntity.ok(new ArrayList<>(types));
    }
    
    @GetMapping("/search")
    public ResponseEntity<List<Map<String, Object>>> searchTemplates(
            @RequestParam String query,
            @RequestParam(required = false) String difficulty) {
        
        String searchQuery = query.toLowerCase();
        List<Map<String, Object>> templates = difficulty != null ? 
            ScenarioTemplates.getTemplatesByDifficulty(difficulty) : 
            ScenarioTemplates.getAllTemplates();
        
        List<Map<String, Object>> results = templates.stream()
            .filter(template -> {
                String name = template.get("name").toString().toLowerCase();
                String description = template.get("description").toString().toLowerCase();
                String type = template.get("type").toString().toLowerCase();
                return name.contains(searchQuery) || 
                       description.contains(searchQuery) || 
                       type.contains(searchQuery);
            })
            .toList();
        
        return ResponseEntity.ok(results);
    }
    
    @GetMapping("/recommended/{teamId}")
    public ResponseEntity<List<Map<String, Object>>> getRecommendedTemplates(
            @PathVariable String teamId,
            @RequestParam(required = false, defaultValue = "5") int limit) {
        
        // For educational purposes, recommend a mix of difficulties
        List<Map<String, Object>> recommendations = new ArrayList<>();
        
        // Get some beginner templates
        List<Map<String, Object>> beginnerTemplates = ScenarioTemplates.getBeginnerTemplates();
        if (beginnerTemplates.size() > 0) {
            recommendations.add(beginnerTemplates.get(0));
            if (beginnerTemplates.size() > 3) {
                recommendations.add(beginnerTemplates.get(3));
            }
        }
        
        // Get some intermediate templates
        List<Map<String, Object>> intermediateTemplates = ScenarioTemplates.getIntermediateTemplates();
        if (intermediateTemplates.size() > 0) {
            recommendations.add(intermediateTemplates.get(0));
            if (intermediateTemplates.size() > 4) {
                recommendations.add(intermediateTemplates.get(4));
            }
        }
        
        // Get one advanced template
        List<Map<String, Object>> advancedTemplates = ScenarioTemplates.getAdvancedTemplates();
        if (advancedTemplates.size() > 0) {
            recommendations.add(advancedTemplates.get(0));
        }
        
        // Limit results
        if (recommendations.size() > limit) {
            recommendations = recommendations.subList(0, limit);
        }
        
        return ResponseEntity.ok(recommendations);
    }
    
    private Map<String, Long> getCountByField(List<Map<String, Object>> templates, String field) {
        Map<String, Long> counts = new HashMap<>();
        templates.forEach(template -> {
            String value = template.get(field).toString();
            counts.put(value, counts.getOrDefault(value, 0L) + 1);
        });
        return counts;
    }
}