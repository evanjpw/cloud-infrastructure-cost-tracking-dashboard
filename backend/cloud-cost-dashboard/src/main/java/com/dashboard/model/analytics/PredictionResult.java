package com.dashboard.model.analytics;

import java.util.List;
import java.util.Map;

public class PredictionResult {
    private String method;
    private List<Map<String, Object>> predictions;
    private double confidence;
    private Map<String, Object> metadata;
    private String generatedAt;

    // Constructors
    public PredictionResult() {
        this.generatedAt = java.time.Instant.now().toString();
    }

    public PredictionResult(String method, List<Map<String, Object>> predictions, double confidence) {
        this();
        this.method = method;
        this.predictions = predictions;
        this.confidence = confidence;
    }

    // Getters and Setters
    public String getMethod() { return method; }
    public void setMethod(String method) { this.method = method; }

    public List<Map<String, Object>> getPredictions() { return predictions; }
    public void setPredictions(List<Map<String, Object>> predictions) { this.predictions = predictions; }

    public double getConfidence() { return confidence; }
    public void setConfidence(double confidence) { this.confidence = confidence; }

    public Map<String, Object> getMetadata() { return metadata; }
    public void setMetadata(Map<String, Object> metadata) { this.metadata = metadata; }

    public String getGeneratedAt() { return generatedAt; }
    public void setGeneratedAt(String generatedAt) { this.generatedAt = generatedAt; }
}