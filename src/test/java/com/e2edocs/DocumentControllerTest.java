package com.e2edocs;

import com.e2edocs.dto.DocumentDetailResponse;
import com.e2edocs.dto.DocumentResponse;
import com.e2edocs.dto.PageResponse;
import com.e2edocs.entity.enums.DocumentSource;
import com.e2edocs.entity.enums.DocumentStatus;
import com.e2edocs.entity.enums.Priority;
import com.e2edocs.service.DocumentService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class DocumentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DocumentService documentService;

    @Test
    @WithMockUser(username = "aravindgd45@gmail.com", roles = {"SUPER_ADMIN"})
    @DisplayName("GET /api/documents returns frozen pagination envelope { data: [], total: number }")
    void testGetDocumentsPaginationEnvelope() throws Exception {
        DocumentResponse doc = new DocumentResponse();
        doc.setId("DOC-2026-0847");
        doc.setName("Contract_2026_014.pdf");
        doc.setStatus(DocumentStatus.REVIEW);
        doc.setPriority(Priority.HIGH);
        doc.setSource(DocumentSource.EMAIL);

        PageResponse<DocumentResponse> pageResp = new PageResponse<>(Collections.singletonList(doc), 1);

        when(documentService.getDocuments(any(), any(), any(), any(), any(), any(), anyInt(), anyInt(), anyString(), anyString()))
                .thenReturn(pageResp);

        mockMvc.perform(get("/api/documents")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].id").value("DOC-2026-0847"))
                .andExpect(jsonPath("$.data[0].name").value("Contract_2026_014.pdf"))
                .andExpect(jsonPath("$.total").value(1));
    }

    @Test
    @WithMockUser(username = "aravindgd45@gmail.com", roles = {"SUPER_ADMIN"})
    @DisplayName("GET /api/documents/export returns CSV file with proper headers and content")
    void testExportDocumentsCsv() throws Exception {
        String csvContent = "Document ID,Filename,Type,Source,Status,Priority,Department,Assigned User,Sender Email,Sender Name,Created At,Updated At\n"
                + "\"DOC-001\",\"Contract.pdf\",\"Contract\",\"upload\",\"approved\",\"high\",\"Legal\",\"Alex\",\"sender@test.com\",\"Sender\",\"2026-10-02T10:00:00Z\",\"2026-10-02T10:00:00Z\"\n";

        when(documentService.exportDocumentsCsv(any(), any(), any(), any(), any(), any(), anyString(), anyString()))
                .thenReturn(csvContent.getBytes(StandardCharsets.UTF_8));

        mockMvc.perform(get("/api/documents/export")
                        .param("status", "APPROVED"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"e2edocs-documents.csv\""))
                .andExpect(header().string("Content-Type", "text/csv; charset=UTF-8"))
                .andExpect(content().string(csvContent));
    }

    @Test
    @WithMockUser(username = "aravindgd45@gmail.com", roles = {"SUPER_ADMIN"})
    @DisplayName("POST /api/documents multipart upload calls createDocument and returns HTTP 201")
    void testUploadDocumentMultipart() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "invoice.pdf",
                "application/pdf",
                "%PDF-1.4 sample content".getBytes(StandardCharsets.UTF_8)
        );

        DocumentResponse docResp = new DocumentResponse();
        docResp.setId("DOC-999");
        docResp.setName("invoice.pdf");
        docResp.setStatus(DocumentStatus.DRAFT);

        when(documentService.createDocument(any(), any(), any(), any(), any())).thenReturn(docResp);

        mockMvc.perform(multipart("/api/documents")
                        .file(file)
                        .param("type", "Invoice")
                        .param("priority", "HIGH"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("DOC-999"))
                .andExpect(jsonPath("$.name").value("invoice.pdf"));
    }

    @Test
    @WithMockUser(username = "aravindgd45@gmail.com", roles = {"SUPER_ADMIN"})
    @DisplayName("POST /api/documents/{id}/send-email sends document attachment via email")
    void testSendDocumentEmailSuccess() throws Exception {
        doNothing().when(documentService).sendDocumentByEmail(eq("DOC-001"), any(), any(), any(), any());

        String jsonBody = "{\"recipient\":\"partner@example.com\",\"subject\":\"Signed Agreement\",\"message\":\"Please find the document attached.\"}";

        mockMvc.perform(post("/api/documents/DOC-001/send-email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Document successfully sent via email to partner@example.com"));

        verify(documentService, times(1)).sendDocumentByEmail(eq("DOC-001"), any(), any(), any(), any());
    }

    @Test
    @WithMockUser(username = "aravindgd45@gmail.com", roles = {"SUPER_ADMIN"})
    @DisplayName("POST /api/documents/{id}/send-email rejects invalid recipient email")
    void testSendDocumentEmailInvalidRecipient() throws Exception {
        String jsonBody = "{\"recipient\":\"not-an-email\",\"subject\":\"Signed Agreement\",\"message\":\"Attached\"}";

        mockMvc.perform(post("/api/documents/DOC-001/send-email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "aravindgd45@gmail.com", roles = {"SUPER_ADMIN"})
    @DisplayName("GET /api/documents/{id}/download returns stored binary file attachment")
    void testDownloadDocument() throws Exception {
        byte[] content = "Binary document content".getBytes(StandardCharsets.UTF_8);
        ByteArrayResource resource = new ByteArrayResource(content) {
            @Override
            public String getFilename() {
                return "sample.pdf";
            }
        };

        when(documentService.downloadDocument("DOC-001")).thenReturn(resource);

        mockMvc.perform(get("/api/documents/DOC-001/download"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"sample.pdf\""))
                .andExpect(content().bytes(content));
    }
}
