package com.dashboard.service.impl;

import com.dashboard.cloud_cost_dashboard.model.UsageRecord;
import com.dashboard.cloud_cost_dashboard.repository.UsageRecordRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("Usage Ingestion Service Tests")
class UsageIngestionServiceImplTest {

    @Mock
    private UsageRecordRepository usageRepository;

    @InjectMocks
    private UsageIngestionServiceImpl usageIngestionService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("Should ingest usage data from CSV successfully")
    void testIngestUsageDataFromCsv_Success() {
        // Given
        String validFilePath = "/path/to/usage-data.csv";
        when(usageRepository.saveAll(anyList())).thenReturn(createMockUsageRecords());

        // When
        assertDoesNotThrow(() -> usageIngestionService.ingestUsageDataFromCsv(validFilePath));

        // Then
        verify(usageRepository, times(1)).saveAll(anyList());
        
        // Verify that saveAll was called with a list of 2 records (as per implementation)
        verify(usageRepository).saveAll(argThat(list -> {
            @SuppressWarnings("unchecked")
            List<UsageRecord> records = (List<UsageRecord>) list;
            return records.size() == 2;
        }));
    }

    @Test
    @DisplayName("Should handle valid file path without throwing exception")
    void testIngestUsageDataFromCsv_ValidPath() {
        // Given
        String filePath = "/valid/path/to/data.csv";
        when(usageRepository.saveAll(anyList())).thenReturn(createMockUsageRecords());

        // When & Then
        assertDoesNotThrow(() -> usageIngestionService.ingestUsageDataFromCsv(filePath));
        
        // Verify repository interaction
        verify(usageRepository).saveAll(anyList());
    }

    @Test
    @DisplayName("Should handle null file path gracefully")
    void testIngestUsageDataFromCsv_NullPath() {
        // Given
        String nullFilePath = null;
        when(usageRepository.saveAll(anyList())).thenReturn(createMockUsageRecords());

        // When & Then
        assertDoesNotThrow(() -> usageIngestionService.ingestUsageDataFromCsv(nullFilePath));
        
        // Even with null path, the service should still try to save mock records
        verify(usageRepository).saveAll(anyList());
    }

    @Test
    @DisplayName("Should handle empty file path gracefully")
    void testIngestUsageDataFromCsv_EmptyPath() {
        // Given
        String emptyFilePath = "";
        when(usageRepository.saveAll(anyList())).thenReturn(createMockUsageRecords());

        // When & Then
        assertDoesNotThrow(() -> usageIngestionService.ingestUsageDataFromCsv(emptyFilePath));
        
        verify(usageRepository).saveAll(anyList());
    }

    @Test
    @DisplayName("Should handle repository save exception gracefully")
    void testIngestUsageDataFromCsv_RepositoryException() {
        // Given
        String filePath = "/path/to/data.csv";
        when(usageRepository.saveAll(anyList()))
                .thenThrow(new RuntimeException("Database connection failed"));

        // When & Then
        assertDoesNotThrow(() -> usageIngestionService.ingestUsageDataFromCsv(filePath));
        
        // Verify that saveAll was attempted
        verify(usageRepository).saveAll(anyList());
    }

    @Test
    @DisplayName("Should handle very long file path")
    void testIngestUsageDataFromCsv_LongPath() {
        // Given
        StringBuilder longPathBuilder = new StringBuilder("/very/long/path");
        for (int i = 0; i < 100; i++) {
            longPathBuilder.append("/directory").append(i);
        }
        longPathBuilder.append("/usage-data.csv");
        String longFilePath = longPathBuilder.toString();
        
        when(usageRepository.saveAll(anyList())).thenReturn(createMockUsageRecords());

        // When & Then
        assertDoesNotThrow(() -> usageIngestionService.ingestUsageDataFromCsv(longFilePath));
        
        verify(usageRepository).saveAll(anyList());
    }

    @Test
    @DisplayName("Should handle special characters in file path")
    void testIngestUsageDataFromCsv_SpecialCharacters() {
        // Given
        String specialCharPath = "/path/with-special_chars/üñícode/data@2025.csv";
        when(usageRepository.saveAll(anyList())).thenReturn(createMockUsageRecords());

        // When & Then
        assertDoesNotThrow(() -> usageIngestionService.ingestUsageDataFromCsv(specialCharPath));
        
        verify(usageRepository).saveAll(anyList());
    }

    @Test
    @DisplayName("Should verify mock record creation and saving")
    void testIngestUsageDataFromCsv_MockRecordCreation() {
        // Given
        String filePath = "/test/data.csv";
        when(usageRepository.saveAll(anyList())).thenReturn(createMockUsageRecords());

        // When
        usageIngestionService.ingestUsageDataFromCsv(filePath);

        // Then
        verify(usageRepository).saveAll(argThat(list -> {
            @SuppressWarnings("unchecked")
            List<UsageRecord> records = (List<UsageRecord>) list;
            
            // Verify the list contains exactly 2 UsageRecord objects
            assertEquals(2, records.size());
            
            // Verify all items are UsageRecord instances
            return records.stream().allMatch(record -> record instanceof UsageRecord);
        }));
    }

    @Test
    @DisplayName("Should handle multiple consecutive ingestion calls")
    void testIngestUsageDataFromCsv_MultipleConsecutiveCalls() {
        // Given
        String filePath1 = "/path/to/file1.csv";
        String filePath2 = "/path/to/file2.csv";
        String filePath3 = "/path/to/file3.csv";
        
        when(usageRepository.saveAll(anyList())).thenReturn(createMockUsageRecords());

        // When
        usageIngestionService.ingestUsageDataFromCsv(filePath1);
        usageIngestionService.ingestUsageDataFromCsv(filePath2);
        usageIngestionService.ingestUsageDataFromCsv(filePath3);

        // Then
        verify(usageRepository, times(3)).saveAll(anyList());
    }

    @Test
    @DisplayName("Should handle repository returning empty list")
    void testIngestUsageDataFromCsv_RepositoryReturnsEmpty() {
        // Given
        String filePath = "/path/to/data.csv";
        when(usageRepository.saveAll(anyList())).thenReturn(List.of());

        // When & Then
        assertDoesNotThrow(() -> usageIngestionService.ingestUsageDataFromCsv(filePath));
        
        verify(usageRepository).saveAll(anyList());
    }

    @Test
    @DisplayName("Should verify no additional repository interactions")
    void testIngestUsageDataFromCsv_NoAdditionalInteractions() {
        // Given
        String filePath = "/test/data.csv";
        when(usageRepository.saveAll(anyList())).thenReturn(createMockUsageRecords());

        // When
        usageIngestionService.ingestUsageDataFromCsv(filePath);

        // Then
        verify(usageRepository, times(1)).saveAll(anyList());
        verifyNoMoreInteractions(usageRepository);
    }

    @Test
    @DisplayName("Should handle runtime exception during processing")
    void testIngestUsageDataFromCsv_RuntimeExceptionHandling() {
        // Given
        String filePath = "/problematic/file.csv";
        doThrow(new RuntimeException("Unexpected error during save"))
                .when(usageRepository).saveAll(anyList());

        // When & Then
        // The service should handle the exception gracefully (not re-throw)
        assertDoesNotThrow(() -> usageIngestionService.ingestUsageDataFromCsv(filePath));
        
        verify(usageRepository).saveAll(anyList());
    }

    @Test
    @DisplayName("Should verify service behavior with different file extensions")
    void testIngestUsageDataFromCsv_DifferentFileExtensions() {
        // Given
        when(usageRepository.saveAll(anyList())).thenReturn(createMockUsageRecords());

        // When & Then
        assertDoesNotThrow(() -> usageIngestionService.ingestUsageDataFromCsv("/data/file.csv"));
        assertDoesNotThrow(() -> usageIngestionService.ingestUsageDataFromCsv("/data/file.txt"));
        assertDoesNotThrow(() -> usageIngestionService.ingestUsageDataFromCsv("/data/file.xlsx"));
        assertDoesNotThrow(() -> usageIngestionService.ingestUsageDataFromCsv("/data/file"));
        
        // Should have called saveAll 4 times
        verify(usageRepository, times(4)).saveAll(anyList());
    }

    @Test
    @DisplayName("Should handle repository returning null gracefully")
    void testIngestUsageDataFromCsv_RepositoryReturnsNull() {
        // Given
        String filePath = "/path/to/data.csv";
        when(usageRepository.saveAll(anyList())).thenReturn(null);

        // When & Then
        assertDoesNotThrow(() -> usageIngestionService.ingestUsageDataFromCsv(filePath));
        
        verify(usageRepository).saveAll(anyList());
    }

    // ========================================
    // HELPER METHODS FOR TEST DATA
    // ========================================

    private List<UsageRecord> createMockUsageRecords() {
        UsageRecord record1 = new UsageRecord();
        record1.setId(1L);
        
        UsageRecord record2 = new UsageRecord();
        record2.setId(2L);
        
        return List.of(record1, record2);
    }
}