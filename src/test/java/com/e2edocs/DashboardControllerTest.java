package com.e2edocs;

import com.e2edocs.dto.DashboardStatsResponse;
import com.e2edocs.repository.DocumentRepository;
import com.e2edocs.service.DashboardService;
import com.e2edocs.service.DocumentService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class DashboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DashboardService dashboardService;

    @MockBean
    private DocumentRepository documentRepository;

    @MockBean
    private DocumentService documentService;

    @Test
    @WithMockUser(username = "sarah.chen@example.com", roles = {"SUPER_ADMIN"})
    @DisplayName("GET /api/dashboard/stats returns stats structure")
    void testGetDashboardStats() throws Exception {
        DashboardStatsResponse stats = new DashboardStatsResponse();
        stats.setTotalDocuments(42);
        stats.setProcessing(5);
        stats.setApproved(20);
        stats.setReview(10);
        stats.setRejected(2);
        stats.setDraft(5);
        stats.setCriticalItems(1);
        stats.setActiveRules(3);
        stats.setActiveWorkflows(2);

        when(dashboardService.getStats(anyString())).thenReturn(stats);

        mockMvc.perform(get("/api/dashboard/stats")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalDocuments").value(42))
                .andExpect(jsonPath("$.approved").value(20))
                .andExpect(jsonPath("$.criticalItems").value(1));
    }
}
