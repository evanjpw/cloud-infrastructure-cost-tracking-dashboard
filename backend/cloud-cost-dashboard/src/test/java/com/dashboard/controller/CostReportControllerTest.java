package com.dashboard.controller;

import com.dashboard.dto.CostReportRequest;
import com.dashboard.service.interfaces.ReportGenerationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.beans.factory.annotation.Autowired;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import static org.springframework.http.MediaType.APPLICATION_JSON;

@WebMvcTest(CostReportController.class)
class CostReportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ReportGenerationService reportGenerationService;

    @BeforeEach
    void setUp() {
        // Setup can be left empty for now
    }

    @Test
    void generateCostReport_shouldReturn200() throws Exception {
        String requestBody = """
            {
                "teamName": "Platform",
                "startDate": "2024-11-01",
                "endDate": "2024-11-07"
            }
        """;

        mockMvc.perform(post("/api/reports")
                .contentType(APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk());

        System.out.println("POST /api/reports returned 200 OK");
    }
}
