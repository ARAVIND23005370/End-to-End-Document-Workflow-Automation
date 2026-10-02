package com.e2edocs;

import com.e2edocs.dto.WorkflowResponse;
import com.e2edocs.dto.WorkflowStepDto;
import com.e2edocs.entity.enums.WorkflowStatus;
import com.e2edocs.service.WorkflowService;
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

import java.util.Collections;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class WorkflowControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private WorkflowService workflowService;

    @Test
    @WithMockUser(username = "sarah.chen@example.com", roles = {"SUPER_ADMIN"})
    @DisplayName("GET /api/workflows returns list of workflows")
    void testGetWorkflows() throws Exception {
        WorkflowResponse wf = new WorkflowResponse();
        wf.setId("wf-001");
        wf.setName("Standard Document Intake");
        wf.setStatus(WorkflowStatus.ACTIVE);
        wf.setSteps(Collections.singletonList(new WorkflowStepDto("ws-0", "Ingestion", "trigger", "completed", 1)));

        when(workflowService.getAllWorkflows(anyString())).thenReturn(Collections.singletonList(wf));

        mockMvc.perform(get("/api/workflows")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("wf-001"))
                .andExpect(jsonPath("$[0].name").value("Standard Document Intake"))
                .andExpect(jsonPath("$[0].steps[0].name").value("Ingestion"));
    }
}
