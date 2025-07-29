package com.dashboard.dto.education;

import java.time.LocalDateTime;

public class AttemptDTO {
    private Long id;
    private String studentId;
    private String scenarioId;
    private String scenarioName;
    private String scenarioType;
    private String difficultyLevel;
    private String status;
    private Double score;
    private String grade;
    private Double actualSavingsAchieved;
    private Double targetSavings;
    private Integer timeTakenMinutes;
    private Integer attemptNumber;
    private String feedback;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    
    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }
    public String getScenarioId() { return scenarioId; }
    public void setScenarioId(String scenarioId) { this.scenarioId = scenarioId; }
    public String getScenarioName() { return scenarioName; }
    public void setScenarioName(String scenarioName) { this.scenarioName = scenarioName; }
    public String getScenarioType() { return scenarioType; }
    public void setScenarioType(String scenarioType) { this.scenarioType = scenarioType; }
    public String getDifficultyLevel() { return difficultyLevel; }
    public void setDifficultyLevel(String difficultyLevel) { this.difficultyLevel = difficultyLevel; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Double getScore() { return score; }
    public void setScore(Double score) { this.score = score; }
    public String getGrade() { return grade; }
    public void setGrade(String grade) { this.grade = grade; }
    public Double getActualSavingsAchieved() { return actualSavingsAchieved; }
    public void setActualSavingsAchieved(Double actualSavingsAchieved) { this.actualSavingsAchieved = actualSavingsAchieved; }
    public Double getTargetSavings() { return targetSavings; }
    public void setTargetSavings(Double targetSavings) { this.targetSavings = targetSavings; }
    public Integer getTimeTakenMinutes() { return timeTakenMinutes; }
    public void setTimeTakenMinutes(Integer timeTakenMinutes) { this.timeTakenMinutes = timeTakenMinutes; }
    public Integer getAttemptNumber() { return attemptNumber; }
    public void setAttemptNumber(Integer attemptNumber) { this.attemptNumber = attemptNumber; }
    public String getFeedback() { return feedback; }
    public void setFeedback(String feedback) { this.feedback = feedback; }
    public LocalDateTime getStartedAt() { return startedAt; }
    public void setStartedAt(LocalDateTime startedAt) { this.startedAt = startedAt; }
    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }
}