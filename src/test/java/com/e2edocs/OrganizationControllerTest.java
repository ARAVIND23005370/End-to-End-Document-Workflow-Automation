package com.e2edocs;

import com.e2edocs.dto.DepartmentResponse;
import com.e2edocs.dto.DocumentCategoryResponse;
import com.e2edocs.dto.TeamResponse;
import com.e2edocs.service.OrganizationService;
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
class OrganizationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OrganizationService organizationService;

    @Test
    @WithMockUser(username = "sarah.chen@example.com", roles = {"SUPER_ADMIN"})
    @DisplayName("GET /api/organization/departments returns dynamic departments from database")
    void testGetDepartments() throws Exception {
        DepartmentResponse dept = new DepartmentResponse("dept-001", "Operations", "OPS", "Operations team");
        when(organizationService.getDepartments(anyString())).thenReturn(Collections.singletonList(dept));

        mockMvc.perform(get("/api/organization/departments")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("dept-001"))
                .andExpect(jsonPath("$[0].name").value("Operations"))
                .andExpect(jsonPath("$[0].code").value("OPS"));
    }

    @Test
    @WithMockUser(username = "sarah.chen@example.com", roles = {"SUPER_ADMIN"})
    @DisplayName("GET /api/organization/teams returns dynamic teams")
    void testGetTeams() throws Exception {
        TeamResponse team = new TeamResponse("team-001", "Document Intake Team", "dept-001", "u-002");
        when(organizationService.getTeams(anyString())).thenReturn(Collections.singletonList(team));

        mockMvc.perform(get("/api/organization/teams")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("team-001"))
                .andExpect(jsonPath("$[0].name").value("Document Intake Team"));
    }

    @Test
    @WithMockUser(username = "sarah.chen@example.com", roles = {"SUPER_ADMIN"})
    @DisplayName("GET /api/organization/categories returns dynamic categories")
    void testGetCategories() throws Exception {
        DocumentCategoryResponse cat = new DocumentCategoryResponse("cat-001", "Service Agreement", "Contracts and SLAs");
        when(organizationService.getCategories(anyString())).thenReturn(Collections.singletonList(cat));

        mockMvc.perform(get("/api/organization/categories")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("cat-001"))
                .andExpect(jsonPath("$[0].name").value("Service Agreement"));
    }
}
