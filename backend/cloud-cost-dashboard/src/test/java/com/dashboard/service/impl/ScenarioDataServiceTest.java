package com.dashboard.service.impl;

import com.dashboard.templates.ScenarioTemplates;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ScenarioDataServiceTest {
    
    @Mock
    private JdbcTemplate jdbcTemplate;
    
    @InjectMocks
    private ScenarioDataService scenarioDataService;
    
    @BeforeEach
    void setUp() {
        // Mock successful database operations by default
        when(jdbcTemplate.update(anyString(), any(Object[].class))).thenReturn(1);
    }
    
    @Test
    @DisplayName("Should generate scenario session successfully for beginner template")
    void testGenerateScenarioSession_Beginner() {
        // Given
        String templateId = "beginner_rightsizing_ec2";
        String studentIdentifier = "student123";
        
        // When
        String sessionId = scenarioDataService.generateScenarioSession(templateId, studentIdentifier);
        
        // Then
        assertNotNull(sessionId);
        assertTrue(sessionId.matches("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$"));
        
        // Verify session creation
        verify(jdbcTemplate).update(
            contains("INSERT INTO scenario_sessions"),
            eq(sessionId),
            eq(templateId),
            eq("Right-size Over-provisioned EC2 Instances"),
            eq("beginner"),
            eq("rightsizing"),
            eq(studentIdentifier),
            any()
        );
        
        // Verify batch operations were called
        verify(jdbcTemplate, atLeastOnce()).batchUpdate(anyString(), anyList(), anyInt(), any());
    }
    
    @Test
    @DisplayName("Should generate scenario with appropriate data volume for difficulty")
    void testGenerateScenarioSession_DataVolumeByDifficulty() {
        // Test beginner
        String sessionId = scenarioDataService.generateScenarioSession("beginner_ebs_cleanup", "test1");
        assertNotNull(sessionId);
        
        // Test intermediate
        sessionId = scenarioDataService.generateScenarioSession("intermediate_multi_region", "test2");
        assertNotNull(sessionId);
        
        // Test advanced
        sessionId = scenarioDataService.generateScenarioSession("advanced_multi_cloud", "test3");
        assertNotNull(sessionId);
        
        // Verify more data generated for higher difficulties
        verify(jdbcTemplate, atLeast(3)).batchUpdate(anyString(), anyList(), anyInt(), any());
    }
    
    @Test
    @DisplayName("Should throw exception for invalid template ID")
    void testGenerateScenarioSession_InvalidTemplate() {
        // Given
        String invalidTemplateId = "invalid_template";
        String studentIdentifier = "student123";
        
        // When/Then
        assertThrows(IllegalArgumentException.class, () -> {
            scenarioDataService.generateScenarioSession(invalidTemplateId, studentIdentifier);
        });
    }
    
    @Test
    @DisplayName("Should generate grading keys with appropriate hints")
    void testGenerateScenarioSession_GradingKeys() {
        // Given
        String templateId = "beginner_rightsizing_ec2";
        String studentIdentifier = "student123";
        
        // When
        String sessionId = scenarioDataService.generateScenarioSession(templateId, studentIdentifier);
        
        // Then
        // Verify grading keys were inserted
        verify(jdbcTemplate).batchUpdate(
            contains("INSERT INTO grading_keys"),
            anyList(),
            anyInt(),
            any()
        );
    }
    
    @Test
    @DisplayName("Should handle all scenario types")
    void testGenerateScenarioSession_AllScenarioTypes() {
        // Test each scenario type
        String[] scenarioTypes = {
            "beginner_rightsizing_ec2",
            "beginner_ebs_cleanup",
            "intermediate_ri_optimization",
            "intermediate_spot_fleet",
            "advanced_multi_cloud"
        };
        
        for (String templateId : scenarioTypes) {
            Map<String, Object> template = ScenarioTemplates.getTemplateById(templateId);
            if (template != null) {
                String sessionId = scenarioDataService.generateScenarioSession(templateId, "test_" + templateId);
                assertNotNull(sessionId);
            }
        }
    }
    
    @Test
    @DisplayName("Should generate metadata with correct calculations")
    void testGenerateScenarioSession_Metadata() {
        // Given
        String templateId = "beginner_dev_scheduling";
        String studentIdentifier = "student456";
        
        // When
        String sessionId = scenarioDataService.generateScenarioSession(templateId, studentIdentifier);
        
        // Then
        // Verify metadata insertion
        verify(jdbcTemplate).update(
            contains("INSERT INTO scenario_metadata"),
            eq(sessionId),
            anyDouble(), // average savings percentage
            anyString(), // config JSON
            eq(sessionId)
        );
    }
    
    @Test
    @DisplayName("Should generate realistic cost data with inefficiencies")
    void testGenerateScenarioSession_CostInefficiencies() {
        // Given
        String templateId = "intermediate_container_packing";
        String studentIdentifier = "student789";
        
        // When
        String sessionId = scenarioDataService.generateScenarioSession(templateId, studentIdentifier);
        
        // Then
        assertNotNull(sessionId);
        
        // Verify usage data was generated with proper batch size
        verify(jdbcTemplate, atLeastOnce()).batchUpdate(
            contains("INSERT INTO scenario_usage_data"),
            anyList(),
            anyInt(),
            any()
        );
    }
}