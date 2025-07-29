package com.dashboard.model.education;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.*;

@Entity
@Table(name = "students")
public class Student {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String studentId;
    
    @Column(nullable = false)
    private String name;
    
    @Column(unique = true, nullable = false)
    private String email;
    
    @Column(name = "course_id")
    private String courseId;
    
    @Column(name = "enrollment_date")
    private LocalDateTime enrollmentDate;
    
    @Column(name = "skill_level")
    private String skillLevel; // beginner, intermediate, advanced
    
    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<StudentProgress> progressRecords = new ArrayList<>();
    
    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ScenarioAttempt> scenarioAttempts = new ArrayList<>();
    
    @ElementCollection
    @CollectionTable(name = "student_achievements", joinColumns = @JoinColumn(name = "student_id"))
    @Column(name = "achievement")
    private Set<String> achievements = new HashSet<>();
    
    @Column(name = "total_scenarios_completed")
    private Integer totalScenariosCompleted = 0;
    
    @Column(name = "total_savings_achieved")
    private Double totalSavingsAchieved = 0.0;
    
    @Column(name = "average_score")
    private Double averageScore = 0.0;
    
    @Column(name = "last_active")
    private LocalDateTime lastActive;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        enrollmentDate = LocalDateTime.now();
        lastActive = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
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
    
    public LocalDateTime getEnrollmentDate() {
        return enrollmentDate;
    }
    
    public void setEnrollmentDate(LocalDateTime enrollmentDate) {
        this.enrollmentDate = enrollmentDate;
    }
    
    public String getSkillLevel() {
        return skillLevel;
    }
    
    public void setSkillLevel(String skillLevel) {
        this.skillLevel = skillLevel;
    }
    
    public List<StudentProgress> getProgressRecords() {
        return progressRecords;
    }
    
    public void setProgressRecords(List<StudentProgress> progressRecords) {
        this.progressRecords = progressRecords;
    }
    
    public List<ScenarioAttempt> getScenarioAttempts() {
        return scenarioAttempts;
    }
    
    public void setScenarioAttempts(List<ScenarioAttempt> scenarioAttempts) {
        this.scenarioAttempts = scenarioAttempts;
    }
    
    public Set<String> getAchievements() {
        return achievements;
    }
    
    public void setAchievements(Set<String> achievements) {
        this.achievements = achievements;
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
    
    public LocalDateTime getLastActive() {
        return lastActive;
    }
    
    public void setLastActive(LocalDateTime lastActive) {
        this.lastActive = lastActive;
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