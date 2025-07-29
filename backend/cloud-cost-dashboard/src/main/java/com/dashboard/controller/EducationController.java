package com.dashboard.controller;

import com.dashboard.service.interfaces.EducationService;
import com.dashboard.dto.education.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/education")
@CrossOrigin(origins = "*")
public class EducationController {
    
    @Autowired
    private EducationService educationService;
    
    // Student Management Endpoints
    
    @PostMapping("/students")
    public ResponseEntity<StudentDTO> createStudent(@RequestBody CreateStudentRequest request) {
        StudentDTO student = educationService.createStudent(request);
        return ResponseEntity.ok(student);
    }
    
    @GetMapping("/students/{studentId}")
    public ResponseEntity<StudentDTO> getStudent(@PathVariable String studentId) {
        StudentDTO student = educationService.getStudent(studentId);
        return ResponseEntity.ok(student);
    }
    
    @PutMapping("/students/{studentId}")
    public ResponseEntity<StudentDTO> updateStudent(
            @PathVariable String studentId, 
            @RequestBody UpdateStudentRequest request) {
        StudentDTO student = educationService.updateStudent(studentId, request);
        return ResponseEntity.ok(student);
    }
    
    @GetMapping("/courses/{courseId}/students")
    public ResponseEntity<List<StudentDTO>> getStudentsByCourse(@PathVariable String courseId) {
        List<StudentDTO> students = educationService.getStudentsByCourse(courseId);
        return ResponseEntity.ok(students);
    }
    
    @GetMapping("/students/{studentId}/statistics")
    public ResponseEntity<StudentStatsDTO> getStudentStatistics(@PathVariable String studentId) {
        StudentStatsDTO stats = educationService.getStudentStatistics(studentId);
        return ResponseEntity.ok(stats);
    }
    
    // Progress Tracking Endpoints
    
    @PutMapping("/students/{studentId}/progress")
    public ResponseEntity<ProgressDTO> updateProgress(
            @PathVariable String studentId, 
            @RequestBody UpdateProgressRequest request) {
        ProgressDTO progress = educationService.updateProgress(studentId, request);
        return ResponseEntity.ok(progress);
    }
    
    @GetMapping("/students/{studentId}/progress")
    public ResponseEntity<List<ProgressDTO>> getStudentProgress(@PathVariable String studentId) {
        List<ProgressDTO> progress = educationService.getStudentProgress(studentId);
        return ResponseEntity.ok(progress);
    }
    
    @GetMapping("/students/{studentId}/progress/summary")
    public ResponseEntity<ProgressSummaryDTO> getProgressSummary(@PathVariable String studentId) {
        ProgressSummaryDTO summary = educationService.getProgressSummary(studentId);
        return ResponseEntity.ok(summary);
    }
    
    @PostMapping("/students/{studentId}/modules/{moduleId}/checkpoints/{checkpoint}")
    public ResponseEntity<Void> recordCheckpoint(
            @PathVariable String studentId,
            @PathVariable String moduleId,
            @PathVariable String checkpoint) {
        educationService.recordCheckpoint(studentId, moduleId, checkpoint);
        return ResponseEntity.ok().build();
    }
    
    // Scenario Attempt Endpoints
    
    @PostMapping("/students/{studentId}/attempts")
    public ResponseEntity<AttemptDTO> startScenarioAttempt(
            @PathVariable String studentId,
            @RequestBody StartAttemptRequest request) {
        AttemptDTO attempt = educationService.startScenarioAttempt(studentId, request);
        return ResponseEntity.ok(attempt);
    }
    
    @PutMapping("/students/{studentId}/attempts/{attemptId}/complete")
    public ResponseEntity<AttemptDTO> completeScenarioAttempt(
            @PathVariable String studentId,
            @PathVariable Long attemptId,
            @RequestBody CompleteAttemptRequest request) {
        AttemptDTO attempt = educationService.completeScenarioAttempt(studentId, attemptId, request);
        return ResponseEntity.ok(attempt);
    }
    
    @GetMapping("/students/{studentId}/attempts")
    public ResponseEntity<List<AttemptDTO>> getStudentAttempts(@PathVariable String studentId) {
        List<AttemptDTO> attempts = educationService.getStudentAttempts(studentId);
        return ResponseEntity.ok(attempts);
    }
    
    @GetMapping("/attempts/{attemptId}/analysis")
    public ResponseEntity<AttemptAnalysisDTO> analyzeAttempt(@PathVariable Long attemptId) {
        AttemptAnalysisDTO analysis = educationService.analyzeAttempt(attemptId);
        return ResponseEntity.ok(analysis);
    }
    
    // Grading and Assessment Endpoints
    
    @PostMapping("/attempts/{attemptId}/grade")
    public ResponseEntity<GradeDTO> gradeScenarioAttempt(
            @PathVariable Long attemptId,
            @RequestBody GradeRequest request) {
        GradeDTO grade = educationService.gradeScenarioAttempt(attemptId, request);
        return ResponseEntity.ok(grade);
    }
    
    @GetMapping("/students/{studentId}/grades")
    public ResponseEntity<List<GradeDTO>> getStudentGrades(@PathVariable String studentId) {
        List<GradeDTO> grades = educationService.getStudentGrades(studentId);
        return ResponseEntity.ok(grades);
    }
    
    @GetMapping("/students/{studentId}/transcript")
    public ResponseEntity<TranscriptDTO> generateTranscript(@PathVariable String studentId) {
        TranscriptDTO transcript = educationService.generateTranscript(studentId);
        return ResponseEntity.ok(transcript);
    }
    
    // Analytics Endpoints
    
    @GetMapping("/students/{studentId}/analytics")
    public ResponseEntity<LearningAnalyticsDTO> getStudentAnalytics(@PathVariable String studentId) {
        LearningAnalyticsDTO analytics = educationService.getStudentAnalytics(studentId);
        return ResponseEntity.ok(analytics);
    }
    
    @GetMapping("/courses/{courseId}/analytics")
    public ResponseEntity<CourseAnalyticsDTO> getCourseAnalytics(@PathVariable String courseId) {
        CourseAnalyticsDTO analytics = educationService.getCourseAnalytics(courseId);
        return ResponseEntity.ok(analytics);
    }
    
    @GetMapping("/scenarios/{scenarioId}/analytics")
    public ResponseEntity<Map<String, Object>> getScenarioAnalytics(@PathVariable String scenarioId) {
        Map<String, Object> analytics = educationService.getScenarioAnalytics(scenarioId);
        return ResponseEntity.ok(analytics);
    }
    
    // Achievement Endpoints
    
    @PostMapping("/students/{studentId}/achievements/check")
    public ResponseEntity<List<String>> checkAndAwardAchievements(@PathVariable String studentId) {
        List<String> newAchievements = educationService.checkAndAwardAchievements(studentId);
        return ResponseEntity.ok(newAchievements);
    }
    
    @GetMapping("/achievements")
    public ResponseEntity<List<AchievementDTO>> getAvailableAchievements() {
        List<AchievementDTO> achievements = educationService.getAvailableAchievements();
        return ResponseEntity.ok(achievements);
    }
    
    @GetMapping("/students/{studentId}/achievements")
    public ResponseEntity<List<String>> getStudentAchievements(@PathVariable String studentId) {
        List<String> achievements = educationService.getStudentAchievements(studentId);
        return ResponseEntity.ok(achievements);
    }
    
    // Leaderboard Endpoints
    
    @GetMapping("/leaderboard")
    public ResponseEntity<List<LeaderboardEntryDTO>> getLeaderboard(
            @RequestParam(defaultValue = "score") String type,
            @RequestParam(defaultValue = "global") String scope,
            @RequestParam(defaultValue = "10") int limit) {
        List<LeaderboardEntryDTO> leaderboard = educationService.getLeaderboard(type, scope, limit);
        return ResponseEntity.ok(leaderboard);
    }
    
    @GetMapping("/students/{studentId}/rank")
    public ResponseEntity<Integer> getStudentRank(
            @PathVariable String studentId,
            @RequestParam(defaultValue = "score") String type) {
        Integer rank = educationService.getStudentRank(studentId, type);
        return ResponseEntity.ok(rank);
    }
    
    // Recommendation Endpoints
    
    @GetMapping("/students/{studentId}/recommendations/scenarios")
    public ResponseEntity<List<Map<String, Object>>> getRecommendedScenarios(@PathVariable String studentId) {
        List<Map<String, Object>> recommendations = educationService.getRecommendedScenarios(studentId);
        return ResponseEntity.ok(recommendations);
    }
    
    @GetMapping("/students/{studentId}/learning-path")
    public ResponseEntity<String> getNextLearningPath(@PathVariable String studentId) {
        String path = educationService.getNextLearningPath(studentId);
        return ResponseEntity.ok(path);
    }
    
    @GetMapping("/students/{studentId}/skill-gaps")
    public ResponseEntity<List<String>> getSkillGapAnalysis(@PathVariable String studentId) {
        List<String> skillGaps = educationService.getSkillGapAnalysis(studentId);
        return ResponseEntity.ok(skillGaps);
    }
    
    // Instructor Dashboard Endpoints
    
    @GetMapping("/instructor/dashboard")
    public ResponseEntity<Map<String, Object>> getInstructorDashboard(
            @RequestParam(required = false) String courseId) {
        // Provide overview data for instructors
        Map<String, Object> dashboard = Map.of(
            "message", "Instructor dashboard functionality available",
            "courseId", courseId != null ? courseId : "all",
            "features", List.of(
                "Course analytics",
                "Student progress tracking", 
                "Grade management",
                "Scenario performance analysis"
            )
        );
        return ResponseEntity.ok(dashboard);
    }
    
    @GetMapping("/instructor/export/{courseId}")
    public ResponseEntity<Map<String, Object>> exportCourseData(@PathVariable String courseId) {
        // Export course data for external analysis
        Map<String, Object> exportData = Map.of(
            "courseId", courseId,
            "exportedAt", java.time.LocalDateTime.now(),
            "message", "Course data export functionality available",
            "dataTypes", List.of(
                "Student roster",
                "Grade book",
                "Progress reports",
                "Analytics data"
            )
        );
        return ResponseEntity.ok(exportData);
    }
}