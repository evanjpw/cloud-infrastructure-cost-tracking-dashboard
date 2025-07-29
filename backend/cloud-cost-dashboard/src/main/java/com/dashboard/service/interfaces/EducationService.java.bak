package com.dashboard.service.interfaces;

import com.dashboard.dto.education.*;
import com.dashboard.model.education.*;

import java.util.List;
import java.util.Map;

public interface EducationService {
    
    // Student Management
    StudentDTO createStudent(CreateStudentRequest request);
    StudentDTO getStudent(String studentId);
    StudentDTO updateStudent(String studentId, UpdateStudentRequest request);
    List<StudentDTO> getStudentsByCourse(String courseId);
    StudentStatsDTO getStudentStatistics(String studentId);
    
    // Progress Tracking
    ProgressDTO updateProgress(String studentId, UpdateProgressRequest request);
    List<ProgressDTO> getStudentProgress(String studentId);
    ProgressSummaryDTO getProgressSummary(String studentId);
    void recordCheckpoint(String studentId, String moduleId, String checkpoint);
    
    // Scenario Attempts
    AttemptDTO startScenarioAttempt(String studentId, StartAttemptRequest request);
    AttemptDTO completeScenarioAttempt(String studentId, Long attemptId, CompleteAttemptRequest request);
    List<AttemptDTO> getStudentAttempts(String studentId);
    AttemptAnalysisDTO analyzeAttempt(Long attemptId);
    
    // Grading and Assessment
    GradeDTO gradeScenarioAttempt(Long attemptId, GradeRequest request);
    List<GradeDTO> getStudentGrades(String studentId);
    TranscriptDTO generateTranscript(String studentId);
    
    // Learning Analytics
    LearningAnalyticsDTO getStudentAnalytics(String studentId);
    CourseAnalyticsDTO getCourseAnalytics(String courseId);
    Map<String, Object> getScenarioAnalytics(String scenarioId);
    
    // Achievements and Badges
    List<String> checkAndAwardAchievements(String studentId);
    List<AchievementDTO> getAvailableAchievements();
    List<String> getStudentAchievements(String studentId);
    
    // Leaderboard
    List<LeaderboardEntryDTO> getLeaderboard(String type, String scope, int limit);
    Integer getStudentRank(String studentId, String type);
    
    // Recommendations
    List<Map<String, Object>> getRecommendedScenarios(String studentId);
    String getNextLearningPath(String studentId);
    List<String> getSkillGapAnalysis(String studentId);
}