package com.dashboard.repository;

import com.dashboard.model.education.ScenarioAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ScenarioAttemptRepository extends JpaRepository<ScenarioAttempt, Long> {
    
    List<ScenarioAttempt> findByStudentId(Long studentId);
    
    List<ScenarioAttempt> findByScenarioId(String scenarioId);
    
    List<ScenarioAttempt> findByStudentIdAndScenarioId(Long studentId, String scenarioId);
    
    List<ScenarioAttempt> findByStatus(String status);
    
    List<ScenarioAttempt> findByDifficultyLevel(String difficultyLevel);
    
    @Query("SELECT a FROM ScenarioAttempt a WHERE a.student.id = :studentId AND a.status = :status")
    List<ScenarioAttempt> findByStudentIdAndStatus(@Param("studentId") Long studentId, @Param("status") String status);
    
    @Query("SELECT a FROM ScenarioAttempt a WHERE a.score >= :minScore")
    List<ScenarioAttempt> findHighScoringAttempts(@Param("minScore") Double minScore);
    
    @Query("SELECT a FROM ScenarioAttempt a WHERE a.completedAt BETWEEN :startDate AND :endDate")
    List<ScenarioAttempt> findAttemptsInDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT AVG(a.score) FROM ScenarioAttempt a WHERE a.scenarioId = :scenarioId AND a.status = 'completed'")
    Double getAverageScoreForScenario(@Param("scenarioId") String scenarioId);
    
    @Query("SELECT COUNT(DISTINCT a.student.id) FROM ScenarioAttempt a WHERE a.scenarioId = :scenarioId")
    Long countUniqueStudentsForScenario(@Param("scenarioId") String scenarioId);
    
    @Query("SELECT a FROM ScenarioAttempt a WHERE a.student.id = :studentId ORDER BY a.completedAt DESC")
    List<ScenarioAttempt> findRecentAttemptsForStudent(@Param("studentId") Long studentId);
    
    @Query("SELECT a.scenarioType, COUNT(a) FROM ScenarioAttempt a WHERE a.student.id = :studentId GROUP BY a.scenarioType")
    List<Object[]> getScenarioTypeDistributionForStudent(@Param("studentId") Long studentId);
}