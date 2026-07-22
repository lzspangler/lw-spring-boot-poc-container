package com.redhat.lightwell.integration;

import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class DocumentIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldUploadDocument() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "test-document.pdf", "application/pdf",
                "sample PDF content".getBytes(StandardCharsets.UTF_8));

        mockMvc.perform(multipart("/api/accounts/1/documents")
                        .file(file)
                        .with(httpBasic("admin", "admin123")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.fileName").value("test-document.pdf"))
                .andExpect(jsonPath("$.contentType").value("application/pdf"))
                .andExpect(jsonPath("$.fileSize").isNumber())
                .andExpect(jsonPath("$.accountId").value(1))
                .andExpect(jsonPath("$.uploadedAt").isNotEmpty());
    }

    @Test
    void shouldListDocumentsAfterUpload() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "statement.pdf", "application/pdf",
                "statement content".getBytes(StandardCharsets.UTF_8));

        mockMvc.perform(multipart("/api/accounts/1/documents")
                        .file(file)
                        .with(httpBasic("admin", "admin123")))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/accounts/1/documents")
                        .with(httpBasic("admin", "admin123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].fileName").value("statement.pdf"));
    }

    @Test
    void shouldReturn404WhenUploadToNonExistentAccount() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "doc.pdf", "application/pdf",
                "content".getBytes(StandardCharsets.UTF_8));

        mockMvc.perform(multipart("/api/accounts/999/documents")
                        .file(file)
                        .with(httpBasic("admin", "admin123")))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturn404WhenListDocumentsForNonExistentAccount() throws Exception {
        mockMvc.perform(get("/api/accounts/999/documents")
                        .with(httpBasic("admin", "admin123")))
                .andExpect(status().isNotFound());
    }
}
