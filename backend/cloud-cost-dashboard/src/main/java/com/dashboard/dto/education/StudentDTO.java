package com.dashboard.dto.education;

import java.time.LocalDateTime;
import java.util.List;

public class StudentDTO {
    private Long id;
    private String studentId;
    private String name;
    private String email;
    private String courseId;
    private String skillLevel;
    private LocalDateTime enrollmentDate;
    private LocalDateTime lastActive;
    private Integer totalScenariosCompleted;
    private Double totalSavingsAchieved;
    private Double averageScore;
    private List<String> achievements;
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getStudentId() {
        return studentId;
    }
    
    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getCourseId() {
        return courseId;
    }
    
    public void setCourseId(String courseId) {
        this.courseId = courseId;
    }
    
    public String getSkillLevel() {
        return skillLevel;
    }
    
    public void setSkillLevel(String skillLevel) {
        this.skillLevel = skillLevel;
    }
    
    public LocalDateTime getEnrollmentDate() {
        return enrollmentDate;
    }
    
    public void setEnrollmentDate(LocalDateTime enrollmentDate) {
        this.enrollmentDate = enrollmentDate;
    }
    
    public LocalDateTime getLastActive() {
        return lastActive;
    }
    
    public void setLastActive(LocalDateTime lastActive) {
        this.lastActive = lastActive;
    }
    
    public Integer getTotalScenariosCompleted() {
        return totalScenariosCompleted;
    }
    
    public void setTotalScenariosCompleted(Integer totalScenariosCompleted) {
        this.totalScenariosCompleted = totalScenariosCompleted;
    }
    
    public Double getTotalSavingsAchieved() {
        return totalSavingsAchieved;
    }
    
    public void setTotalSavingsAchieved(Double totalSavingsAchieved) {
        this.totalSavingsAchieved = totalSavingsAchieved;
    }
    
    public Double getAverageScore() {
        return averageScore;
    }
    
    public void setAverageScore(Double averageScore) {
        this.averageScore = averageScore;
    }
    
    public List<String> getAchievements() {
        return achievements;
    }
    
    public void setAchievements(List<String> achievements) {
        this.achievements = achievements;
    }
}