package com.redhat.lightwell.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class DataQueryIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldQuerySimpleJsonField() throws Exception {
        mockMvc.perform(post("/api/admin/query")
                        .with(httpBasic("admin", "admin123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"data\":\"{\\\"name\\\":\\\"John\\\",\\\"age\\\":30}\","
                                + "\"expression\":\"$.name\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.expression").value("$.name"))
                .andExpect(jsonPath("$.result").value("John"))
                .andExpect(jsonPath("$.resultType").value("String"));
    }

    @Test
    void shouldQueryNestedJsonField() throws Exception {
        mockMvc.perform(post("/api/admin/query")
                        .with(httpBasic("admin", "admin123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"data\":\"{\\\"user\\\":{\\\"profile\\\":{\\\"city\\\":\\\"Austin\\\"}}}\","
                                + "\"expression\":\"$.user.profile.city\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("Austin"));
    }

    @Test
    void shouldReturnNullForNonExistentPath() throws Exception {
        mockMvc.perform(post("/api/admin/query")
                        .with(httpBasic("admin", "admin123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"data\":\"{\\\"name\\\":\\\"John\\\"}\","
                                + "\"expression\":\"$.nonexistent\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultType").value("null"));
    }

    @Test
    void shouldReturn400WhenDataIsNull() throws Exception {
        mockMvc.perform(post("/api/admin/query")
                        .with(httpBasic("admin", "admin123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"data\":null,\"expression\":\"$.name\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn400WhenExpressionIsBlank() throws Exception {
        mockMvc.perform(post("/api/admin/query")
                        .with(httpBasic("admin", "admin123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"data\":\"{}\",\"expression\":\"\"}"))
                .andExpect(status().isBadRequest());
    }
}
