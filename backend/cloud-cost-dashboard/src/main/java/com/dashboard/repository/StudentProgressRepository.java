package com.dashboard.repository;

import com.dashboard.model.education.StudentProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentProgressRepository extends JpaRepository<StudentProgress, Long> {
    
    List<StudentProgress> findByStudentId(Long studentId);
    
    Optional<StudentProgress> findByStudentIdAndModuleId(Long studentId, String moduleId);
    
    List<StudentProgress> findByModuleId(String moduleId);
    
    List<StudentProgress> findByStatus(String status);
    
    @Query("SELECT p FROM StudentProgress p WHERE p.student.id = :studentId AND p.status = :status")
    List<StudentProgress> findByStudentIdAndStatus(@Param("studentId") Long studentId, @Param("status") String status);
    
    @Query("SELECT p FROM StudentProgress p WHERE p.progressPercentage >= :minProgress")
    List<StudentProgress> findWithMinimumProgress(@Param("minProgress") Double minProgress);
    
    @Query("SELECT AVG(p.progressPercentage) FROM StudentProgress p WHERE p.student.id = :studentId")
    Double getAverageProgressForStudent(@Param("studentId") Long studentId);
    
    @Query("SELECT COUNT(p) FROM StudentProgress p WHERE p.student.id = :studentId AND p.status = 'completed'")
    Long countCompletedModulesForStudent(@Param("studentId") Long studentId);
    
    @Query("SELECT SUM(p.timeSpentMinutes) FROM StudentProgress p WHERE p.student.id = :studentId")
    Integer getTotalTimeSpentByStudent(@Param("studentId") Long studentId);
}