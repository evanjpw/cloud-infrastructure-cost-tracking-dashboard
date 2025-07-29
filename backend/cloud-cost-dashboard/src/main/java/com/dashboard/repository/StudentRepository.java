package com.dashboard.repository;

import com.dashboard.model.education.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {
    
    Optional<Student> findByStudentId(String studentId);
    
    Optional<Student> findByEmail(String email);
    
    List<Student> findByCourseId(String courseId);
    
    List<Student> findBySkillLevel(String skillLevel);
    
    @Query("SELECT s FROM Student s WHERE s.lastActive >= :date")
    List<Student> findActiveStudentsSince(@Param("date") LocalDateTime date);
    
    @Query("SELECT s FROM Student s WHERE s.totalScenariosCompleted >= :minScenarios")
    List<Student> findStudentsWithMinimumCompletedScenarios(@Param("minScenarios") Integer minScenarios);
    
    @Query("SELECT s FROM Student s WHERE s.averageScore >= :minScore")
    List<Student> findTopPerformers(@Param("minScore") Double minScore);
    
    @Query("SELECT s FROM Student s ORDER BY s.totalSavingsAchieved DESC")
    List<Student> findTopSavers();
    
    @Query("SELECT COUNT(s) FROM Student s WHERE s.courseId = :courseId")
    Long countStudentsInCourse(@Param("courseId") String courseId);
    
    @Query("SELECT AVG(s.averageScore) FROM Student s WHERE s.courseId = :courseId")
    Double getAverageScoreForCourse(@Param("courseId") String courseId);
}