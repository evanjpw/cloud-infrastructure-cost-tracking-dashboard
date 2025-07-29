package com.dashboard.model.education;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Entity
@Table(name = "scenario_attempts")
public class ScenarioAttempt {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;
    
    @Column(name = "scenario_id", nullable = false)
    private String scenarioId;
    
    @Column(name = "scenario_name")
    private String scenarioName;
    
    @Column(name = "scenario_type")
    private String scenarioType;
    
    @Column(name = "difficulty_level")
    private String difficultyLevel;
    
    @Column(name = "status")
    private String status; // started, completed, abandoned
    
    @Column(name = "score")
    private Double score;
    
    @Column(name = "max_score")
    private Double maxScore = 100.0;
    
    @Column(name = "actual_savings_achieved")
    private Double actualSavingsAchieved;
    
    @Column(name = "target_savings")
    private Double targetSavings;
    
    @Column(name = "time_taken_minutes")
    private Integer timeTakenMinutes;
    
    @Column(name = "attempt_number")
    private Integer attemptNumber = 1;
    
    @ElementCollection
    @CollectionTable(name = "attempt_metrics", joinColumns = @JoinColumn(name = "attempt_id"))
    @MapKeyColumn(name = "metric_name")
    @Column(name = "metric_value")
    private Map<String, String> performanceMetrics = new HashMap<>();
    
    @Column(name = "feedback", columnDefinition = "TEXT")
    private String feedback;
    
    @Column(name = "grade")
    private String grade; // A+, A, B+, B, C, F
    
    @Column(name = "started_at")
    private LocalDateTime startedAt;
    
    @Column(name = "completed_at")
    private LocalDateTime completedAt;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        startedAt = LocalDateTime.now();
        if (status == null) {
            status = "started";
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    // Calculate grade based on score
    public void calculateGrade() {
        if (score == null || maxScore == null) {
            grade = "N/A";
            return;
        }
        
        double percentage = (score / maxScore) * 100;
        
        if (percentage >= 95) {
            grade = "A+";
        } else if (percentage >= 90) {
            grade = "A";
        } else if (percentage >= 85) {
            grade = "B+";
        } else if (percentage >= 80) {
            grade = "B";
        } else if (percentage >= 70) {
            grade = "C";
        } else {
            grade = "F";
        }
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Student getStudent() {
        return student;
    }
    
    public void setStudent(Student student) {
        this.student = student;
    }
    
    public String getScenarioId() {
        return scenarioId;
    }
    
    public void setScenarioId(String scenarioId) {
        this.scenarioId = scenarioId;
    }
    
    public String getScenarioName() {
        return scenarioName;
    }
    
    public void setScenarioName(String scenarioName) {
        this.scenarioName = scenarioName;
    }
    
    public String getScenarioType() {
        return scenarioType;
    }
    
    public void setScenarioType(String scenarioType) {
        this.scenarioType = scenarioType;
    }
    
    public String getDifficultyLevel() {
        return difficultyLevel;
    }
    
    public void setDifficultyLevel(String difficultyLevel) {
        this.difficultyLevel = difficultyLevel;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public Double getScore() {
        return score;
    }
    
    public void setScore(Double score) {
        this.score = score;
        calculateGrade();
    }
    
    public Double getMaxScore() {
        return maxScore;
    }
    
    public void setMaxScore(Double maxScore) {
        this.maxScore = maxScore;
        calculateGrade();
    }
    
    public Double getActualSavingsAchieved() {
        return actualSavingsAchieved;
    }
    
    public void setActualSavingsAchieved(Double actualSavingsAchieved) {
        this.actualSavingsAchieved = actualSavingsAchieved;
    }
    
    public Double getTargetSavings() {
        return targetSavings;
    }
    
    public void setTargetSavings(Double targetSavings) {
        this.targetSavings = targetSavings;
    }
    
    public Integer getTimeTakenMinutes() {
        return timeTakenMinutes;
    }
    
    public void setTimeTakenMinutes(Integer timeTakenMinutes) {
        this.timeTakenMinutes = timeTakenMinutes;
    }
    
    public Integer getAttemptNumber() {
        return attemptNumber;
    }
    
    public void setAttemptNumber(Integer attemptNumber) {
        this.attemptNumber = attemptNumber;
    }
    
    public Map<String, String> getPerformanceMetrics() {
        return performanceMetrics;
    }
    
    public void setPerformanceMetrics(Map<String, String> performanceMetrics) {
        this.performanceMetrics = performanceMetrics;
    }
    
    public String getFeedback() {
        return feedback;
    }
    
    public void setFeedback(String feedback) {
        this.feedback = feedback;
    }
    
    public String getGrade() {
        return grade;
    }
    
    public void setGrade(String grade) {
        this.grade = grade;
    }
    
    public LocalDateTime getStartedAt() {
        return startedAt;
    }
    
    public void setStartedAt(LocalDateTime startedAt) {
        this.startedAt = startedAt;
    }
    
    public LocalDateTime getCompletedAt() {
        return completedAt;
    }
    
    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}