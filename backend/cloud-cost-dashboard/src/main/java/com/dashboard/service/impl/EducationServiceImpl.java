package com.dashboard.service.impl;

import com.dashboard.service.interfaces.EducationService;
import com.dashboard.dto.education.*;
import com.dashboard.model.education.*;
import com.dashboard.repository.*;
import com.dashboard.templates.ScenarioTemplates;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class EducationServiceImpl implements EducationService {
    
    @Autowired
    private StudentRepository studentRepository;
    
    @Autowired
    private StudentProgressRepository progressRepository;
    
    @Autowired
    private ScenarioAttemptRepository attemptRepository;
    
    @Override
    public StudentDTO createStudent(CreateStudentRequest request) {
        // Check if student already exists
        if (studentRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Student with email " + request.getEmail() + " already exists");
        }
        
        Student student = new Student();
        student.setStudentId(generateStudentId());
        student.setName(request.getName());
        student.setEmail(request.getEmail());
        student.setCourseId(request.getCourseId());
        student.setSkillLevel(request.getSkillLevel() != null ? request.getSkillLevel() : "beginner");
        
        student = studentRepository.save(student);
        
        return convertToStudentDTO(student);
    }
    
    @Override
    public StudentDTO getStudent(String studentId) {
        Student student = studentRepository.findByStudentId(studentId)
            .orElseThrow(() -> new RuntimeException("Student not found: " + studentId));
        
        // Update last active
        student.setLastActive(LocalDateTime.now());
        studentRepository.save(student);
        
        return convertToStudentDTO(student);
    }
    
    @Override
    public StudentDTO updateStudent(String studentId, UpdateStudentRequest request) {
        Student student = studentRepository.findByStudentId(studentId)
            .orElseThrow(() -> new RuntimeException("Student not found: " + studentId));
        
        if (request.getName() != null) {
            student.setName(request.getName());
        }
        if (request.getSkillLevel() != null) {
            student.setSkillLevel(request.getSkillLevel());
        }
        if (request.getCourseId() != null) {
            student.setCourseId(request.getCourseId());
        }
        
        student = studentRepository.save(student);
        
        return convertToStudentDTO(student);
    }
    
    @Override
    public List<StudentDTO> getStudentsByCourse(String courseId) {
        List<Student> students = studentRepository.findByCourseId(courseId);
        return students.stream()
            .map(this::convertToStudentDTO)
            .collect(Collectors.toList());
    }
    
    @Override
    public StudentStatsDTO getStudentStatistics(String studentId) {
        Student student = studentRepository.findByStudentId(studentId)
            .orElseThrow(() -> new RuntimeException("Student not found: " + studentId));
        
        StudentStatsDTO stats = new StudentStatsDTO();
        stats.setStudentId(studentId);
        stats.setTotalScenariosCompleted(student.getTotalScenariosCompleted());
        stats.setTotalSavingsAchieved(student.getTotalSavingsAchieved());
        stats.setAverageScore(student.getAverageScore());
        stats.setSkillLevel(student.getSkillLevel());
        
        // Calculate additional statistics
        List<ScenarioAttempt> attempts = attemptRepository.findByStudentId(student.getId());
        
        // Success rate
        long completedAttempts = attempts.stream()
            .filter(a -> "completed".equals(a.getStatus()))
            .count();
        double successRate = attempts.isEmpty() ? 0.0 : 
            (double) completedAttempts / attempts.size() * 100;
        stats.setSuccessRate(successRate);
        
        // Time statistics
        int totalTimeSpent = attempts.stream()
            .mapToInt(a -> a.getTimeTakenMinutes() != null ? a.getTimeTakenMinutes() : 0)
            .sum();
        stats.setTotalTimeSpentMinutes(totalTimeSpent);
        
        // Difficulty distribution
        Map<String, Long> difficultyDistribution = attempts.stream()
            .filter(a -> a.getDifficultyLevel() != null)
            .collect(Collectors.groupingBy(
                ScenarioAttempt::getDifficultyLevel,
                Collectors.counting()
            ));
        stats.setDifficultyDistribution(difficultyDistribution);
        
        // Recent activity
        attempts.stream()
            .map(ScenarioAttempt::getCompletedAt)
            .filter(Objects::nonNull)
            .max(LocalDateTime::compareTo)
            .ifPresent(stats::setLastActivityDate);
        
        return stats;
    }
    
    @Override
    public ProgressDTO updateProgress(String studentId, UpdateProgressRequest request) {
        Student student = studentRepository.findByStudentId(studentId)
            .orElseThrow(() -> new RuntimeException("Student not found: " + studentId));
        
        StudentProgress progress = progressRepository.findByStudentIdAndModuleId(
            student.getId(), request.getModuleId()
        ).orElseGet(() -> {
            StudentProgress newProgress = new StudentProgress();
            newProgress.setStudent(student);
            newProgress.setModuleId(request.getModuleId());
            newProgress.setModuleName(request.getModuleName());
            return newProgress;
        });
        
        // Update progress
        if (request.getProgressPercentage() != null) {
            progress.setProgressPercentage(request.getProgressPercentage());
        }
        
        if (request.getStatus() != null) {
            progress.setStatus(request.getStatus());
            if ("in_progress".equals(request.getStatus()) && progress.getStartedAt() == null) {
                progress.setStartedAt(LocalDateTime.now());
            } else if ("completed".equals(request.getStatus())) {
                progress.setCompletedAt(LocalDateTime.now());
                progress.setProgressPercentage(100.0);
            }
        }
        
        if (request.getTimeSpentMinutes() != null) {
            progress.setTimeSpentMinutes(
                progress.getTimeSpentMinutes() + request.getTimeSpentMinutes()
            );
        }
        
        if (request.getScore() != null) {
            progress.setScore(request.getScore());
        }
        
        progress = progressRepository.save(progress);
        
        return convertToProgressDTO(progress);
    }
    
    @Override
    public List<ProgressDTO> getStudentProgress(String studentId) {
        Student student = studentRepository.findByStudentId(studentId)
            .orElseThrow(() -> new RuntimeException("Student not found: " + studentId));
        
        List<StudentProgress> progressList = progressRepository.findByStudentId(student.getId());
        
        return progressList.stream()
            .map(this::convertToProgressDTO)
            .collect(Collectors.toList());
    }
    
    @Override
    public ProgressSummaryDTO getProgressSummary(String studentId) {
        Student student = studentRepository.findByStudentId(studentId)
            .orElseThrow(() -> new RuntimeException("Student not found: " + studentId));
        
        List<StudentProgress> progressList = progressRepository.findByStudentId(student.getId());
        
        ProgressSummaryDTO summary = new ProgressSummaryDTO();
        summary.setStudentId(studentId);
        summary.setTotalModules(progressList.size());
        
        long completedModules = progressList.stream()
            .filter(p -> "completed".equals(p.getStatus()))
            .count();
        summary.setCompletedModules((int) completedModules);
        
        long inProgressModules = progressList.stream()
            .filter(p -> "in_progress".equals(p.getStatus()))
            .count();
        summary.setInProgressModules((int) inProgressModules);
        
        double overallProgress = progressList.isEmpty() ? 0.0 :
            progressList.stream()
                .mapToDouble(StudentProgress::getProgressPercentage)
                .average()
                .orElse(0.0);
        summary.setOverallProgressPercentage(overallProgress);
        
        int totalTimeSpent = progressList.stream()
            .mapToInt(StudentProgress::getTimeSpentMinutes)
            .sum();
        summary.setTotalTimeSpentMinutes(totalTimeSpent);
        
        return summary;
    }
    
    @Override
    public void recordCheckpoint(String studentId, String moduleId, String checkpoint) {
        Student student = studentRepository.findByStudentId(studentId)
            .orElseThrow(() -> new RuntimeException("Student not found: " + studentId));
        
        StudentProgress progress = progressRepository.findByStudentIdAndModuleId(
            student.getId(), moduleId
        ).orElseThrow(() -> new RuntimeException("Progress not found for module: " + moduleId));
        
        progress.getCheckpoints().put(checkpoint, true);
        progressRepository.save(progress);
    }
    
    @Override
    public AttemptDTO startScenarioAttempt(String studentId, StartAttemptRequest request) {
        Student student = studentRepository.findByStudentId(studentId)
            .orElseThrow(() -> new RuntimeException("Student not found: " + studentId));
        
        // Get scenario template details
        Map<String, Object> template = ScenarioTemplates.getTemplateById(request.getScenarioId());
        if (template == null) {
            throw new RuntimeException("Scenario template not found: " + request.getScenarioId());
        }
        
        // Count previous attempts
        List<ScenarioAttempt> previousAttempts = attemptRepository.findByStudentIdAndScenarioId(
            student.getId(), request.getScenarioId()
        );
        
        ScenarioAttempt attempt = new ScenarioAttempt();
        attempt.setStudent(student);
        attempt.setScenarioId(request.getScenarioId());
        attempt.setScenarioName((String) template.get("name"));
        attempt.setScenarioType((String) template.get("type"));
        attempt.setDifficultyLevel((String) template.get("difficulty"));
        attempt.setAttemptNumber(previousAttempts.size() + 1);
        attempt.setStatus("started");
        
        // Set target savings based on template
        String estimatedSavings = (String) template.get("estimatedSavings");
        if (estimatedSavings != null && estimatedSavings.contains("-")) {
            String[] range = estimatedSavings.replace("%", "").split("-");
            double targetSavings = (Double.parseDouble(range[0]) + Double.parseDouble(range[1])) / 2;
            attempt.setTargetSavings(targetSavings);
        }
        
        attempt = attemptRepository.save(attempt);
        
        return convertToAttemptDTO(attempt);
    }
    
    @Override
    public AttemptDTO completeScenarioAttempt(String studentId, Long attemptId, CompleteAttemptRequest request) {
        Student student = studentRepository.findByStudentId(studentId)
            .orElseThrow(() -> new RuntimeException("Student not found: " + studentId));
        
        ScenarioAttempt attempt = attemptRepository.findById(attemptId)
            .orElseThrow(() -> new RuntimeException("Attempt not found: " + attemptId));
        
        // Verify the attempt belongs to the student
        if (!attempt.getStudent().getId().equals(student.getId())) {
            throw new RuntimeException("Attempt does not belong to student");
        }
        
        // Update attempt
        attempt.setStatus("completed");
        attempt.setCompletedAt(LocalDateTime.now());
        attempt.setActualSavingsAchieved(request.getActualSavingsAchieved());
        
        // Calculate time taken
        if (attempt.getStartedAt() != null) {
            long minutesTaken = ChronoUnit.MINUTES.between(
                attempt.getStartedAt(), attempt.getCompletedAt()
            );
            attempt.setTimeTakenMinutes((int) minutesTaken);
        }
        
        // Set performance metrics
        if (request.getPerformanceMetrics() != null) {
            attempt.getPerformanceMetrics().putAll(request.getPerformanceMetrics());
        }
        
        // Calculate score
        double score = calculateScore(attempt, request);
        attempt.setScore(score);
        
        // Generate feedback
        String feedback = generateFeedback(attempt, request);
        attempt.setFeedback(feedback);
        
        attempt = attemptRepository.save(attempt);
        
        // Update student statistics
        updateStudentStats(student, attempt);
        
        // Check for achievements
        checkAndAwardAchievements(studentId);
        
        return convertToAttemptDTO(attempt);
    }
    
    @Override
    public List<AttemptDTO> getStudentAttempts(String studentId) {
        Student student = studentRepository.findByStudentId(studentId)
            .orElseThrow(() -> new RuntimeException("Student not found: " + studentId));
        
        List<ScenarioAttempt> attempts = attemptRepository.findByStudentId(student.getId());
        
        return attempts.stream()
            .map(this::convertToAttemptDTO)
            .collect(Collectors.toList());
    }
    
    @Override
    public AttemptAnalysisDTO analyzeAttempt(Long attemptId) {
        ScenarioAttempt attempt = attemptRepository.findById(attemptId)
            .orElseThrow(() -> new RuntimeException("Attempt not found: " + attemptId));
        
        AttemptAnalysisDTO analysis = new AttemptAnalysisDTO();
        analysis.setAttemptId(attemptId);
        analysis.setScenarioId(attempt.getScenarioId());
        analysis.setStudentId(attempt.getStudent().getStudentId());
        
        // Performance analysis
        analysis.setScore(attempt.getScore());
        analysis.setGrade(attempt.getGrade());
        analysis.setTimeTakenMinutes(attempt.getTimeTakenMinutes());
        
        // Savings analysis
        if (attempt.getTargetSavings() != null && attempt.getActualSavingsAchieved() != null) {
            double savingsEfficiency = (attempt.getActualSavingsAchieved() / attempt.getTargetSavings()) * 100;
            analysis.setSavingsEfficiency(savingsEfficiency);
        }
        
        // Compare with other attempts
        Double avgScore = attemptRepository.getAverageScoreForScenario(attempt.getScenarioId());
        analysis.setAverageScoreForScenario(avgScore);
        
        // Strengths and weaknesses
        List<String> strengths = new ArrayList<>();
        List<String> weaknesses = new ArrayList<>();
        
        if (attempt.getScore() != null && attempt.getScore() > 90) {
            strengths.add("Excellent overall performance");
        }
        if (attempt.getTimeTakenMinutes() != null && attempt.getTimeTakenMinutes() < 30) {
            strengths.add("Quick completion time");
        }
        if (attempt.getActualSavingsAchieved() != null && attempt.getActualSavingsAchieved() > attempt.getTargetSavings()) {
            strengths.add("Exceeded savings target");
        }
        
        if (attempt.getScore() != null && attempt.getScore() < 70) {
            weaknesses.add("Score below passing threshold");
        }
        if (attempt.getAttemptNumber() > 3) {
            weaknesses.add("Multiple attempts required");
        }
        
        analysis.setStrengths(strengths);
        analysis.setWeaknesses(weaknesses);
        
        // Recommendations
        List<String> recommendations = generateRecommendations(attempt);
        analysis.setRecommendations(recommendations);
        
        return analysis;
    }
    
    @Override
    public GradeDTO gradeScenarioAttempt(Long attemptId, GradeRequest request) {
        ScenarioAttempt attempt = attemptRepository.findById(attemptId)
            .orElseThrow(() -> new RuntimeException("Attempt not found: " + attemptId));
        
        // Update score if provided
        if (request.getScore() != null) {
            attempt.setScore(request.getScore());
        }
        
        // Update feedback if provided
        if (request.getFeedback() != null) {
            attempt.setFeedback(request.getFeedback());
        }
        
        // Calculate grade
        attempt.calculateGrade();
        
        attempt = attemptRepository.save(attempt);
        
        GradeDTO grade = new GradeDTO();
        grade.setAttemptId(attemptId);
        grade.setStudentId(attempt.getStudent().getStudentId());
        grade.setScenarioId(attempt.getScenarioId());
        grade.setScore(attempt.getScore());
        grade.setGrade(attempt.getGrade());
        grade.setFeedback(attempt.getFeedback());
        grade.setGradedAt(LocalDateTime.now());
        
        return grade;
    }
    
    @Override
    public List<GradeDTO> getStudentGrades(String studentId) {
        Student student = studentRepository.findByStudentId(studentId)
            .orElseThrow(() -> new RuntimeException("Student not found: " + studentId));
        
        List<ScenarioAttempt> attempts = attemptRepository.findByStudentId(student.getId());
        
        return attempts.stream()
            .filter(a -> a.getScore() != null && a.getGrade() != null)
            .map(a -> {
                GradeDTO grade = new GradeDTO();
                grade.setAttemptId(a.getId());
                grade.setStudentId(studentId);
                grade.setScenarioId(a.getScenarioId());
                grade.setScenarioName(a.getScenarioName());
                grade.setScore(a.getScore());
                grade.setGrade(a.getGrade());
                grade.setFeedback(a.getFeedback());
                grade.setGradedAt(a.getCompletedAt());
                return grade;
            })
            .collect(Collectors.toList());
    }
    
    @Override
    public TranscriptDTO generateTranscript(String studentId) {
        Student student = studentRepository.findByStudentId(studentId)
            .orElseThrow(() -> new RuntimeException("Student not found: " + studentId));
        
        TranscriptDTO transcript = new TranscriptDTO();
        transcript.setStudentId(studentId);
        transcript.setStudentName(student.getName());
        transcript.setEmail(student.getEmail());
        transcript.setCourseId(student.getCourseId());
        transcript.setEnrollmentDate(student.getEnrollmentDate());
        transcript.setGeneratedAt(LocalDateTime.now());
        
        // Get all completed attempts
        List<ScenarioAttempt> completedAttempts = attemptRepository.findByStudentIdAndStatus(
            student.getId(), "completed"
        );
        
        // Group by scenario
        Map<String, List<ScenarioAttempt>> attemptsByScenario = completedAttempts.stream()
            .collect(Collectors.groupingBy(ScenarioAttempt::getScenarioId));
        
        List<TranscriptDTO.CourseRecord> courseRecords = new ArrayList<>();
        
        for (Map.Entry<String, List<ScenarioAttempt>> entry : attemptsByScenario.entrySet()) {
            List<ScenarioAttempt> scenarioAttempts = entry.getValue();
            
            // Use best attempt
            ScenarioAttempt bestAttempt = scenarioAttempts.stream()
                .max(Comparator.comparing(a -> a.getScore() != null ? a.getScore() : 0.0))
                .orElse(null);
            
            if (bestAttempt != null) {
                TranscriptDTO.CourseRecord record = new TranscriptDTO.CourseRecord();
                record.setScenarioId(bestAttempt.getScenarioId());
                record.setScenarioName(bestAttempt.getScenarioName());
                record.setDifficultyLevel(bestAttempt.getDifficultyLevel());
                record.setScore(bestAttempt.getScore());
                record.setGrade(bestAttempt.getGrade());
                record.setCompletedAt(bestAttempt.getCompletedAt());
                record.setAttempts(scenarioAttempts.size());
                courseRecords.add(record);
            }
        }
        
        transcript.setCourseRecords(courseRecords);
        
        // Calculate GPA
        double totalPoints = 0;
        int totalCredits = 0;
        
        for (TranscriptDTO.CourseRecord record : courseRecords) {
            double gradePoints = convertGradeToPoints(record.getGrade());
            int credits = getDifficultyCredits(record.getDifficultyLevel());
            totalPoints += gradePoints * credits;
            totalCredits += credits;
        }
        
        double gpa = totalCredits > 0 ? totalPoints / totalCredits : 0.0;
        transcript.setGpa(gpa);
        transcript.setTotalCredits(totalCredits);
        
        // Overall statistics
        transcript.setTotalScenariosCompleted(student.getTotalScenariosCompleted());
        transcript.setAverageScore(student.getAverageScore());
        transcript.setTotalSavingsAchieved(student.getTotalSavingsAchieved());
        
        return transcript;
    }
    
    @Override
    public LearningAnalyticsDTO getStudentAnalytics(String studentId) {
        Student student = studentRepository.findByStudentId(studentId)
            .orElseThrow(() -> new RuntimeException("Student not found: " + studentId));
        
        LearningAnalyticsDTO analytics = new LearningAnalyticsDTO();
        analytics.setStudentId(studentId);
        
        // Performance trends
        List<ScenarioAttempt> attempts = attemptRepository.findRecentAttemptsForStudent(student.getId());
        
        Map<LocalDateTime, Double> performanceTrend = new LinkedHashMap<>();
        for (ScenarioAttempt attempt : attempts) {
            if (attempt.getCompletedAt() != null && attempt.getScore() != null) {
                performanceTrend.put(attempt.getCompletedAt(), attempt.getScore());
            }
        }
        analytics.setPerformanceTrend(performanceTrend);
        
        // Learning velocity
        long daysActive = ChronoUnit.DAYS.between(student.getEnrollmentDate(), LocalDateTime.now());
        double scenariosPerWeek = daysActive > 0 ? 
            (student.getTotalScenariosCompleted() * 7.0) / daysActive : 0.0;
        analytics.setLearningVelocity(scenariosPerWeek);
        
        // Skill progression
        Map<String, Integer> skillProgression = new HashMap<>();
        List<Object[]> typeDistribution = attemptRepository.getScenarioTypeDistributionForStudent(student.getId());
        for (Object[] row : typeDistribution) {
            skillProgression.put((String) row[0], ((Long) row[1]).intValue());
        }
        analytics.setSkillProgression(skillProgression);
        
        // Strengths and weaknesses
        analytics.setStrengths(identifyStrengths(student, attempts));
        analytics.setAreasForImprovement(identifyWeaknesses(student, attempts));
        
        // Engagement metrics
        analytics.setEngagementScore(calculateEngagementScore(student, attempts));
        analytics.setConsistencyScore(calculateConsistencyScore(student, attempts));
        
        return analytics;
    }
    
    @Override
    public CourseAnalyticsDTO getCourseAnalytics(String courseId) {
        CourseAnalyticsDTO analytics = new CourseAnalyticsDTO();
        analytics.setCourseId(courseId);
        
        // Student statistics
        Long studentCount = studentRepository.countStudentsInCourse(courseId);
        analytics.setTotalStudents(studentCount.intValue());
        
        Double avgScore = studentRepository.getAverageScoreForCourse(courseId);
        analytics.setAverageScore(avgScore != null ? avgScore : 0.0);
        
        // Active students
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        List<Student> activeStudents = studentRepository.findActiveStudentsSince(thirtyDaysAgo);
        long activeCourseStudents = activeStudents.stream()
            .filter(s -> courseId.equals(s.getCourseId()))
            .count();
        analytics.setActiveStudents((int) activeCourseStudents);
        
        // Performance distribution
        List<Student> courseStudents = studentRepository.findByCourseId(courseId);
        Map<String, Integer> gradeDistribution = new HashMap<>();
        
        for (Student student : courseStudents) {
            String gradeCategory = categorizeScore(student.getAverageScore());
            gradeDistribution.put(gradeCategory, gradeDistribution.getOrDefault(gradeCategory, 0) + 1);
        }
        analytics.setGradeDistribution(gradeDistribution);
        
        // Completion rates
        long completedStudents = courseStudents.stream()
            .filter(s -> s.getTotalScenariosCompleted() >= 10) // Assuming 10 scenarios = course completion
            .count();
        double completionRate = courseStudents.isEmpty() ? 0.0 : 
            (double) completedStudents / courseStudents.size() * 100;
        analytics.setCompletionRate(completionRate);
        
        // Popular scenarios
        Map<String, Integer> scenarioPopularity = new HashMap<>();
        for (Student student : courseStudents) {
            List<ScenarioAttempt> attempts = attemptRepository.findByStudentId(student.getId());
            for (ScenarioAttempt attempt : attempts) {
                scenarioPopularity.put(
                    attempt.getScenarioName(), 
                    scenarioPopularity.getOrDefault(attempt.getScenarioName(), 0) + 1
                );
            }
        }
        analytics.setPopularScenarios(scenarioPopularity);
        
        return analytics;
    }
    
    @Override
    public Map<String, Object> getScenarioAnalytics(String scenarioId) {
        Map<String, Object> analytics = new HashMap<>();
        
        analytics.put("scenarioId", scenarioId);
        
        // Basic statistics
        List<ScenarioAttempt> attempts = attemptRepository.findByScenarioId(scenarioId);
        analytics.put("totalAttempts", attempts.size());
        
        Long uniqueStudents = attemptRepository.countUniqueStudentsForScenario(scenarioId);
        analytics.put("uniqueStudents", uniqueStudents);
        
        Double avgScore = attemptRepository.getAverageScoreForScenario(scenarioId);
        analytics.put("averageScore", avgScore != null ? avgScore : 0.0);
        
        // Success rate
        long successfulAttempts = attempts.stream()
            .filter(a -> a.getScore() != null && a.getScore() >= 70.0)
            .count();
        double successRate = attempts.isEmpty() ? 0.0 : 
            (double) successfulAttempts / attempts.size() * 100;
        analytics.put("successRate", successRate);
        
        // Difficulty analysis
        Map<String, Object> difficultyAnalysis = new HashMap<>();
        difficultyAnalysis.put("averageAttempts", calculateAverageAttempts(attempts));
        difficultyAnalysis.put("averageTimeMinutes", calculateAverageTime(attempts));
        analytics.put("difficultyAnalysis", difficultyAnalysis);
        
        // Score distribution
        Map<String, Integer> scoreDistribution = new HashMap<>();
        for (ScenarioAttempt attempt : attempts) {
            if (attempt.getScore() != null) {
                String range = getScoreRange(attempt.getScore());
                scoreDistribution.put(range, scoreDistribution.getOrDefault(range, 0) + 1);
            }
        }
        analytics.put("scoreDistribution", scoreDistribution);
        
        return analytics;
    }
    
    @Override
    public List<String> checkAndAwardAchievements(String studentId) {
        Student student = studentRepository.findByStudentId(studentId)
            .orElseThrow(() -> new RuntimeException("Student not found: " + studentId));
        
        List<String> newAchievements = new ArrayList<>();
        Set<String> currentAchievements = student.getAchievements();
        
        // First Scenario Completed
        if (!currentAchievements.contains("first_scenario") && student.getTotalScenariosCompleted() >= 1) {
            currentAchievements.add("first_scenario");
            newAchievements.add("First Steps - Completed your first scenario!");
        }
        
        // 5 Scenarios Completed
        if (!currentAchievements.contains("five_scenarios") && student.getTotalScenariosCompleted() >= 5) {
            currentAchievements.add("five_scenarios");
            newAchievements.add("Getting Started - Completed 5 scenarios!");
        }
        
        // 10 Scenarios Completed
        if (!currentAchievements.contains("ten_scenarios") && student.getTotalScenariosCompleted() >= 10) {
            currentAchievements.add("ten_scenarios");
            newAchievements.add("Making Progress - Completed 10 scenarios!");
        }
        
        // High Achiever
        if (!currentAchievements.contains("high_achiever") && student.getAverageScore() >= 90.0) {
            currentAchievements.add("high_achiever");
            newAchievements.add("High Achiever - Maintained 90%+ average score!");
        }
        
        // Cost Saver
        if (!currentAchievements.contains("cost_saver_bronze") && student.getTotalSavingsAchieved() >= 1000.0) {
            currentAchievements.add("cost_saver_bronze");
            newAchievements.add("Cost Saver Bronze - Achieved $1,000 in savings!");
        }
        
        if (!currentAchievements.contains("cost_saver_silver") && student.getTotalSavingsAchieved() >= 5000.0) {
            currentAchievements.add("cost_saver_silver");
            newAchievements.add("Cost Saver Silver - Achieved $5,000 in savings!");
        }
        
        if (!currentAchievements.contains("cost_saver_gold") && student.getTotalSavingsAchieved() >= 10000.0) {
            currentAchievements.add("cost_saver_gold");
            newAchievements.add("Cost Saver Gold - Achieved $10,000 in savings!");
        }
        
        // Skill level achievements
        if (!currentAchievements.contains("intermediate_level") && "intermediate".equals(student.getSkillLevel())) {
            currentAchievements.add("intermediate_level");
            newAchievements.add("Level Up - Reached Intermediate skill level!");
        }
        
        if (!currentAchievements.contains("advanced_level") && "advanced".equals(student.getSkillLevel())) {
            currentAchievements.add("advanced_level");
            newAchievements.add("Expert Status - Reached Advanced skill level!");
        }
        
        // Perfect Score
        List<ScenarioAttempt> attempts = attemptRepository.findByStudentId(student.getId());
        boolean hasPerfectScore = attempts.stream()
            .anyMatch(a -> a.getScore() != null && a.getScore() >= 100.0);
        
        if (!currentAchievements.contains("perfect_score") && hasPerfectScore) {
            currentAchievements.add("perfect_score");
            newAchievements.add("Perfection - Achieved a perfect score!");
        }
        
        // Speed Demon - Complete scenario in under 15 minutes
        boolean hasSpeedCompletion = attempts.stream()
            .anyMatch(a -> a.getTimeTakenMinutes() != null && a.getTimeTakenMinutes() < 15);
        
        if (!currentAchievements.contains("speed_demon") && hasSpeedCompletion) {
            currentAchievements.add("speed_demon");
            newAchievements.add("Speed Demon - Completed a scenario in under 15 minutes!");
        }
        
        // Save achievements
        if (!newAchievements.isEmpty()) {
            student.setAchievements(currentAchievements);
            studentRepository.save(student);
        }
        
        return newAchievements;
    }
    
    @Override
    public List<AchievementDTO> getAvailableAchievements() {
        List<AchievementDTO> achievements = new ArrayList<>();
        
        achievements.add(new AchievementDTO("first_scenario", "First Steps", 
            "Complete your first scenario", "bronze", 10));
        achievements.add(new AchievementDTO("five_scenarios", "Getting Started", 
            "Complete 5 scenarios", "bronze", 50));
        achievements.add(new AchievementDTO("ten_scenarios", "Making Progress", 
            "Complete 10 scenarios", "silver", 100));
        achievements.add(new AchievementDTO("high_achiever", "High Achiever", 
            "Maintain 90%+ average score", "gold", 200));
        achievements.add(new AchievementDTO("cost_saver_bronze", "Cost Saver Bronze", 
            "Achieve $1,000 in savings", "bronze", 50));
        achievements.add(new AchievementDTO("cost_saver_silver", "Cost Saver Silver", 
            "Achieve $5,000 in savings", "silver", 100));
        achievements.add(new AchievementDTO("cost_saver_gold", "Cost Saver Gold", 
            "Achieve $10,000 in savings", "gold", 200));
        achievements.add(new AchievementDTO("intermediate_level", "Level Up", 
            "Reach Intermediate skill level", "silver", 100));
        achievements.add(new AchievementDTO("advanced_level", "Expert Status", 
            "Reach Advanced skill level", "gold", 200));
        achievements.add(new AchievementDTO("perfect_score", "Perfection", 
            "Achieve a perfect score", "gold", 150));
        achievements.add(new AchievementDTO("speed_demon", "Speed Demon", 
            "Complete a scenario in under 15 minutes", "silver", 75));
        
        return achievements;
    }
    
    @Override
    public List<String> getStudentAchievements(String studentId) {
        Student student = studentRepository.findByStudentId(studentId)
            .orElseThrow(() -> new RuntimeException("Student not found: " + studentId));
        
        return new ArrayList<>(student.getAchievements());
    }
    
    @Override
    public List<LeaderboardEntryDTO> getLeaderboard(String type, String scope, int limit) {
        List<LeaderboardEntryDTO> leaderboard = new ArrayList<>();
        
        List<Student> students = switch (scope) {
            case "course" -> studentRepository.findByCourseId("default"); // Would need courseId parameter
            case "skill" -> studentRepository.findBySkillLevel("intermediate"); // Would need skill parameter
            default -> studentRepository.findAll();
        };
        
        // Sort based on type
        switch (type) {
            case "score" -> students.sort((a, b) -> 
                Double.compare(b.getAverageScore(), a.getAverageScore()));
            case "scenarios" -> students.sort((a, b) -> 
                Integer.compare(b.getTotalScenariosCompleted(), a.getTotalScenariosCompleted()));
            case "savings" -> students.sort((a, b) -> 
                Double.compare(b.getTotalSavingsAchieved(), a.getTotalSavingsAchieved()));
        }
        
        // Create leaderboard entries
        int rank = 1;
        for (Student student : students) {
            if (rank > limit) break;
            
            LeaderboardEntryDTO entry = new LeaderboardEntryDTO();
            entry.setRank(rank);
            entry.setStudentId(student.getStudentId());
            entry.setStudentName(student.getName());
            entry.setScore(switch (type) {
                case "score" -> student.getAverageScore();
                case "scenarios" -> (double) student.getTotalScenariosCompleted();
                case "savings" -> student.getTotalSavingsAchieved();
                default -> 0.0;
            });
            entry.setAchievementCount(student.getAchievements().size());
            
            leaderboard.add(entry);
            rank++;
        }
        
        return leaderboard;
    }
    
    @Override
    public Integer getStudentRank(String studentId, String type) {
        Student targetStudent = studentRepository.findByStudentId(studentId)
            .orElseThrow(() -> new RuntimeException("Student not found: " + studentId));
        
        List<Student> allStudents = studentRepository.findAll();
        
        // Sort based on type
        switch (type) {
            case "score" -> allStudents.sort((a, b) -> 
                Double.compare(b.getAverageScore(), a.getAverageScore()));
            case "scenarios" -> allStudents.sort((a, b) -> 
                Integer.compare(b.getTotalScenariosCompleted(), a.getTotalScenariosCompleted()));
            case "savings" -> allStudents.sort((a, b) -> 
                Double.compare(b.getTotalSavingsAchieved(), a.getTotalSavingsAchieved()));
        }
        
        // Find rank
        for (int i = 0; i < allStudents.size(); i++) {
            if (allStudents.get(i).getId().equals(targetStudent.getId())) {
                return i + 1;
            }
        }
        
        return null;
    }
    
    @Override
    public List<Map<String, Object>> getRecommendedScenarios(String studentId) {
        Student student = studentRepository.findByStudentId(studentId)
            .orElseThrow(() -> new RuntimeException("Student not found: " + studentId));
        
        List<Map<String, Object>> recommendations = new ArrayList<>();
        
        // Get completed scenarios
        List<ScenarioAttempt> completedAttempts = attemptRepository.findByStudentIdAndStatus(
            student.getId(), "completed"
        );
        Set<String> completedScenarioIds = completedAttempts.stream()
            .map(ScenarioAttempt::getScenarioId)
            .collect(Collectors.toSet());
        
        // Get all templates for student's skill level
        List<Map<String, Object>> templates = ScenarioTemplates.getTemplatesByDifficulty(student.getSkillLevel());
        
        // Filter out completed scenarios and recommend top 5
        templates.stream()
            .filter(t -> !completedScenarioIds.contains(t.get("id")))
            .limit(5)
            .forEach(recommendations::add);
        
        // If not enough scenarios at current level, add some from next level
        if (recommendations.size() < 5) {
            String nextLevel = getNextSkillLevel(student.getSkillLevel());
            if (nextLevel != null) {
                List<Map<String, Object>> nextLevelTemplates = ScenarioTemplates.getTemplatesByDifficulty(nextLevel);
                nextLevelTemplates.stream()
                    .filter(t -> !completedScenarioIds.contains(t.get("id")))
                    .limit(5 - recommendations.size())
                    .forEach(recommendations::add);
            }
        }
        
        return recommendations;
    }
    
    @Override
    public String getNextLearningPath(String studentId) {
        Student student = studentRepository.findByStudentId(studentId)
            .orElseThrow(() -> new RuntimeException("Student not found: " + studentId));
        
        // Analyze completed scenarios
        List<ScenarioAttempt> attempts = attemptRepository.findByStudentId(student.getId());
        Map<String, Long> typeCount = attempts.stream()
            .filter(a -> a.getScenarioType() != null)
            .collect(Collectors.groupingBy(
                ScenarioAttempt::getScenarioType,
                Collectors.counting()
            ));
        
        // Recommend least practiced type
        List<String> allTypes = List.of("cost_optimization", "rightsizing", "reserved_instances", 
                                        "spot_instances", "scaling", "infrastructure_change");
        
        String recommendedType = allTypes.stream()
            .min(Comparator.comparing(type -> typeCount.getOrDefault(type, 0L)))
            .orElse("cost_optimization");
        
        return "Focus on " + recommendedType + " scenarios to build a well-rounded skill set";
    }
    
    @Override
    public List<String> getSkillGapAnalysis(String studentId) {
        Student student = studentRepository.findByStudentId(studentId)
            .orElseThrow(() -> new RuntimeException("Student not found: " + studentId));
        
        List<String> skillGaps = new ArrayList<>();
        
        // Analyze attempts by type
        List<Object[]> typeDistribution = attemptRepository.getScenarioTypeDistributionForStudent(student.getId());
        Map<String, Long> typeCount = new HashMap<>();
        for (Object[] row : typeDistribution) {
            typeCount.put((String) row[0], (Long) row[1]);
        }
        
        // Check for gaps
        if (typeCount.getOrDefault("reserved_instances", 0L) < 2) {
            skillGaps.add("Limited experience with Reserved Instance optimization");
        }
        if (typeCount.getOrDefault("spot_instances", 0L) < 1) {
            skillGaps.add("No experience with Spot Instance strategies");
        }
        if (typeCount.getOrDefault("infrastructure_change", 0L) < 2) {
            skillGaps.add("Limited experience with infrastructure transformations");
        }
        
        // Check performance gaps
        if (student.getAverageScore() < 80.0) {
            skillGaps.add("Overall performance below target - focus on understanding core concepts");
        }
        
        // Check advanced scenarios
        long advancedAttempts = attemptRepository.findByStudentId(student.getId()).stream()
            .filter(a -> "advanced".equals(a.getDifficultyLevel()))
            .count();
        
        if (advancedAttempts == 0 && !"beginner".equals(student.getSkillLevel())) {
            skillGaps.add("Ready to attempt advanced scenarios for skill progression");
        }
        
        return skillGaps;
    }
    
    // Helper methods
    
    private String generateStudentId() {
        return "STU" + System.currentTimeMillis() + (int)(Math.random() * 1000);
    }
    
    private StudentDTO convertToStudentDTO(Student student) {
        StudentDTO dto = new StudentDTO();
        dto.setId(student.getId());
        dto.setStudentId(student.getStudentId());
        dto.setName(student.getName());
        dto.setEmail(student.getEmail());
        dto.setCourseId(student.getCourseId());
        dto.setSkillLevel(student.getSkillLevel());
        dto.setEnrollmentDate(student.getEnrollmentDate());
        dto.setLastActive(student.getLastActive());
        dto.setTotalScenariosCompleted(student.getTotalScenariosCompleted());
        dto.setTotalSavingsAchieved(student.getTotalSavingsAchieved());
        dto.setAverageScore(student.getAverageScore());
        dto.setAchievements(new ArrayList<>(student.getAchievements()));
        return dto;
    }
    
    private ProgressDTO convertToProgressDTO(StudentProgress progress) {
        ProgressDTO dto = new ProgressDTO();
        dto.setId(progress.getId());
        dto.setStudentId(progress.getStudent().getStudentId());
        dto.setModuleId(progress.getModuleId());
        dto.setModuleName(progress.getModuleName());
        dto.setProgressPercentage(progress.getProgressPercentage());
        dto.setStatus(progress.getStatus());
        dto.setScore(progress.getScore());
        dto.setTimeSpentMinutes(progress.getTimeSpentMinutes());
        dto.setCheckpoints(new HashMap<>(progress.getCheckpoints()));
        dto.setStartedAt(progress.getStartedAt());
        dto.setCompletedAt(progress.getCompletedAt());
        dto.setLastAccessed(progress.getLastAccessed());
        return dto;
    }
    
    private AttemptDTO convertToAttemptDTO(ScenarioAttempt attempt) {
        AttemptDTO dto = new AttemptDTO();
        dto.setId(attempt.getId());
        dto.setStudentId(attempt.getStudent().getStudentId());
        dto.setScenarioId(attempt.getScenarioId());
        dto.setScenarioName(attempt.getScenarioName());
        dto.setScenarioType(attempt.getScenarioType());
        dto.setDifficultyLevel(attempt.getDifficultyLevel());
        dto.setStatus(attempt.getStatus());
        dto.setScore(attempt.getScore());
        dto.setGrade(attempt.getGrade());
        dto.setActualSavingsAchieved(attempt.getActualSavingsAchieved());
        dto.setTargetSavings(attempt.getTargetSavings());
        dto.setTimeTakenMinutes(attempt.getTimeTakenMinutes());
        dto.setAttemptNumber(attempt.getAttemptNumber());
        dto.setFeedback(attempt.getFeedback());
        dto.setStartedAt(attempt.getStartedAt());
        dto.setCompletedAt(attempt.getCompletedAt());
        return dto;
    }
    
    private double calculateScore(ScenarioAttempt attempt, CompleteAttemptRequest request) {
        double score = 0.0;
        
        // Base score from savings achieved (40%)
        if (attempt.getTargetSavings() != null && request.getActualSavingsAchieved() != null) {
            double savingsRatio = request.getActualSavingsAchieved() / attempt.getTargetSavings();
            score += Math.min(savingsRatio * 40, 40);
        }
        
        // Time efficiency (20%)
        if (attempt.getTimeTakenMinutes() != null) {
            int expectedTime = getExpectedTime(attempt.getDifficultyLevel());
            double timeRatio = (double) expectedTime / attempt.getTimeTakenMinutes();
            score += Math.min(timeRatio * 20, 20);
        }
        
        // Implementation quality (20%)
        if (request.getImplementationQuality() != null) {
            score += request.getImplementationQuality() * 20;
        }
        
        // Best practices followed (20%)
        if (request.getBestPracticesScore() != null) {
            score += request.getBestPracticesScore() * 20;
        }
        
        return Math.min(score, 100.0);
    }
    
    private String generateFeedback(ScenarioAttempt attempt, CompleteAttemptRequest request) {
        StringBuilder feedback = new StringBuilder();
        
        if (attempt.getScore() >= 90) {
            feedback.append("Excellent work! ");
        } else if (attempt.getScore() >= 80) {
            feedback.append("Good job! ");
        } else if (attempt.getScore() >= 70) {
            feedback.append("Satisfactory performance. ");
        } else {
            feedback.append("Needs improvement. ");
        }
        
        // Specific feedback
        if (attempt.getTargetSavings() != null && request.getActualSavingsAchieved() != null) {
            if (request.getActualSavingsAchieved() >= attempt.getTargetSavings()) {
                feedback.append("You exceeded the savings target! ");
            } else {
                feedback.append("Consider additional optimization strategies to meet savings targets. ");
            }
        }
        
        if (attempt.getTimeTakenMinutes() != null && attempt.getTimeTakenMinutes() > 60) {
            feedback.append("Try to complete scenarios more efficiently. ");
        }
        
        return feedback.toString();
    }
    
    private void updateStudentStats(Student student, ScenarioAttempt attempt) {
        if ("completed".equals(attempt.getStatus())) {
            // Update completed count
            student.setTotalScenariosCompleted(student.getTotalScenariosCompleted() + 1);
            
            // Update savings
            if (attempt.getActualSavingsAchieved() != null) {
                student.setTotalSavingsAchieved(
                    student.getTotalSavingsAchieved() + attempt.getActualSavingsAchieved()
                );
            }
            
            // Update average score
            List<ScenarioAttempt> allCompletedAttempts = attemptRepository.findByStudentIdAndStatus(
                student.getId(), "completed"
            );
            
            double totalScore = allCompletedAttempts.stream()
                .mapToDouble(a -> a.getScore() != null ? a.getScore() : 0.0)
                .sum();
            
            student.setAverageScore(totalScore / allCompletedAttempts.size());
            
            // Check for skill level progression
            if (student.getTotalScenariosCompleted() >= 10 && student.getAverageScore() >= 80.0 
                && "beginner".equals(student.getSkillLevel())) {
                student.setSkillLevel("intermediate");
            } else if (student.getTotalScenariosCompleted() >= 25 && student.getAverageScore() >= 85.0 
                       && "intermediate".equals(student.getSkillLevel())) {
                student.setSkillLevel("advanced");
            }
            
            studentRepository.save(student);
        }
    }
    
    private int getExpectedTime(String difficulty) {
        return switch (difficulty) {
            case "beginner" -> 30;
            case "intermediate" -> 45;
            case "advanced" -> 60;
            default -> 45;
        };
    }
    
    private String getNextSkillLevel(String currentLevel) {
        return switch (currentLevel) {
            case "beginner" -> "intermediate";
            case "intermediate" -> "advanced";
            default -> null;
        };
    }
    
    private double convertGradeToPoints(String grade) {
        return switch (grade) {
            case "A+" -> 4.0;
            case "A" -> 4.0;
            case "B+" -> 3.5;
            case "B" -> 3.0;
            case "C" -> 2.0;
            case "F" -> 0.0;
            default -> 0.0;
        };
    }
    
    private int getDifficultyCredits(String difficulty) {
        return switch (difficulty) {
            case "beginner" -> 1;
            case "intermediate" -> 2;
            case "advanced" -> 3;
            default -> 1;
        };
    }
    
    private String categorizeScore(Double score) {
        if (score == null) return "No Score";
        if (score >= 90) return "A";
        if (score >= 80) return "B";
        if (score >= 70) return "C";
        return "Below C";
    }
    
    private double calculateAverageAttempts(List<ScenarioAttempt> attempts) {
        Map<String, Long> studentAttempts = attempts.stream()
            .collect(Collectors.groupingBy(
                a -> a.getStudent().getStudentId(),
                Collectors.counting()
            ));
        
        return studentAttempts.values().stream()
            .mapToLong(Long::longValue)
            .average()
            .orElse(0.0);
    }
    
    private double calculateAverageTime(List<ScenarioAttempt> attempts) {
        return attempts.stream()
            .filter(a -> a.getTimeTakenMinutes() != null)
            .mapToInt(ScenarioAttempt::getTimeTakenMinutes)
            .average()
            .orElse(0.0);
    }
    
    private String getScoreRange(Double score) {
        if (score >= 90) return "90-100";
        if (score >= 80) return "80-89";
        if (score >= 70) return "70-79";
        if (score >= 60) return "60-69";
        return "Below 60";
    }
    
    private List<String> identifyStrengths(Student student, List<ScenarioAttempt> attempts) {
        List<String> strengths = new ArrayList<>();
        
        if (student.getAverageScore() >= 85) {
            strengths.add("Consistently high performance");
        }
        
        // Check for scenario type strengths
        Map<String, Double> typeAverages = attempts.stream()
            .filter(a -> a.getScore() != null)
            .collect(Collectors.groupingBy(
                ScenarioAttempt::getScenarioType,
                Collectors.averagingDouble(ScenarioAttempt::getScore)
            ));
        
        typeAverages.forEach((type, avg) -> {
            if (avg >= 90) {
                strengths.add("Excellent at " + type + " scenarios");
            }
        });
        
        return strengths;
    }
    
    private List<String> identifyWeaknesses(Student student, List<ScenarioAttempt> attempts) {
        List<String> weaknesses = new ArrayList<>();
        
        if (student.getAverageScore() < 70) {
            weaknesses.add("Overall performance needs improvement");
        }
        
        // Check for scenario type weaknesses
        Map<String, Double> typeAverages = attempts.stream()
            .filter(a -> a.getScore() != null)
            .collect(Collectors.groupingBy(
                ScenarioAttempt::getScenarioType,
                Collectors.averagingDouble(ScenarioAttempt::getScore)
            ));
        
        typeAverages.forEach((type, avg) -> {
            if (avg < 70) {
                weaknesses.add("Struggles with " + type + " scenarios");
            }
        });
        
        return weaknesses;
    }
    
    private double calculateEngagementScore(Student student, List<ScenarioAttempt> attempts) {
        // Based on frequency of attempts and last activity
        long daysSinceEnrollment = ChronoUnit.DAYS.between(student.getEnrollmentDate(), LocalDateTime.now());
        long daysSinceLastActivity = ChronoUnit.DAYS.between(student.getLastActive(), LocalDateTime.now());
        
        double attemptFrequency = daysSinceEnrollment > 0 ? 
            (double) attempts.size() / daysSinceEnrollment : 0.0;
        double recencyScore = Math.max(0, 100 - (daysSinceLastActivity * 5));
        
        return (attemptFrequency * 50 + recencyScore) / 2;
    }
    
    private double calculateConsistencyScore(Student student, List<ScenarioAttempt> attempts) {
        if (attempts.size() < 3) return 0.0;
        
        // Calculate standard deviation of scores
        double avgScore = student.getAverageScore();
        double variance = attempts.stream()
            .filter(a -> a.getScore() != null)
            .mapToDouble(a -> Math.pow(a.getScore() - avgScore, 2))
            .average()
            .orElse(0.0);
        
        double stdDev = Math.sqrt(variance);
        
        // Lower standard deviation = higher consistency
        return Math.max(0, 100 - stdDev);
    }
    
    private List<String> generateRecommendations(ScenarioAttempt attempt) {
        List<String> recommendations = new ArrayList<>();
        
        if (attempt.getScore() < 70) {
            recommendations.add("Review the scenario objectives and try again");
            recommendations.add("Focus on understanding the core concepts before reattempting");
        }
        
        if (attempt.getTimeTakenMinutes() > 60) {
            recommendations.add("Practice similar scenarios to improve speed");
        }
        
        if (attempt.getAttemptNumber() > 2) {
            recommendations.add("Consider reviewing prerequisite materials");
        }
        
        // Type-specific recommendations
        switch (attempt.getScenarioType()) {
            case "rightsizing" -> recommendations.add("Study instance performance metrics analysis");
            case "spot_instances" -> recommendations.add("Learn about spot instance interruption handling");
            case "reserved_instances" -> recommendations.add("Practice capacity planning techniques");
        }
        
        return recommendations;
    }
}