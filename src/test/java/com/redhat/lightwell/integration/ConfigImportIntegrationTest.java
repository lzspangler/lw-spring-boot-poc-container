package com.redhat.lightwell.integration;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class ConfigImportIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @Order(1)
    void shouldReturnEmptyConfigInitially() throws Exception {
        mockMvc.perform(get("/api/admin/config")
                        .with(httpBasic("admin", "admin123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    @Order(2)
    void shouldImportYamlConfig() throws Exception {
        String yaml = "database:\n  host: localhost\n  port: 5432\napp:\n  name: banking\n";

        mockMvc.perform(post("/api/admin/config")
                        .with(httpBasic("admin", "admin123"))
                        .contentType("text/x-yaml")
                        .content(yaml))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.keysImported").value(2));
    }

    @Test
    @Order(3)
    void shouldGetCurrentConfigAfterImport() throws Exception {
        mockMvc.perform(get("/api/admin/config")
                        .with(httpBasic("admin", "admin123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.database").isNotEmpty())
                .andExpect(jsonPath("$.app").isNotEmpty());
    }

    @Test
    @Order(4)
    void shouldDenyUserRoleOnConfigImport() throws Exception {
        mockMvc.perform(post("/api/admin/config")
                        .with(httpBasic("user", "user123"))
                        .contentType("text/x-yaml")
                        .content("key: value\n"))
                .andExpect(status().isForbidden());
    }

    @Test
    @Order(5)
    void shouldDenyUserRoleOnConfigRead() throws Exception {
        mockMvc.perform(get("/api/admin/config")
                        .with(httpBasic("user", "user123")))
                .andExpect(status().isForbidden());
    }

    @Test
    @Order(6)
    void shouldMergeMultipleImports() throws Exception {
        String yaml = "feature:\n  enabled: true\n";

        mockMvc.perform(post("/api/admin/config")
                        .with(httpBasic("admin", "admin123"))
                        .contentType("text/x-yaml")
                        .content(yaml))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/admin/config")
                        .with(httpBasic("admin", "admin123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.database").isNotEmpty())
                .andExpect(jsonPath("$.app").isNotEmpty())
                .andExpect(jsonPath("$.feature").isNotEmpty());
    }
}
