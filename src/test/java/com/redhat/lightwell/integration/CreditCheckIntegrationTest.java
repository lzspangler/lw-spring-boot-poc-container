package com.redhat.lightwell.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.is;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class CreditCheckIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldPerformCreditCheckWithFallbackScore() throws Exception {
        mockMvc.perform(post("/api/customers/1/credit-check")
                        .with(httpBasic("admin", "admin123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customerId").value(1))
                .andExpect(jsonPath("$.score").isNumber())
                .andExpect(jsonPath("$.status", anyOf(is("GOOD"), is("FAIR"))))
                .andExpect(jsonPath("$.provider").value("ExternalCreditBureau"))
                .andExpect(jsonPath("$.checkedAt").isNotEmpty());
    }

    @Test
    void shouldReturnDeterministicScoreForSameCustomer() throws Exception {
        String response1 = mockMvc.perform(post("/api/customers/1/credit-check")
                        .with(httpBasic("admin", "admin123")))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        String response2 = mockMvc.perform(post("/api/customers/1/credit-check")
                        .with(httpBasic("admin", "admin123")))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        JsonNode node1 = objectMapper.readTree(response1);
        JsonNode node2 = objectMapper.readTree(response2);
        assertThat(node1.get("score").asInt()).isEqualTo(node2.get("score").asInt());
    }

    @Test
    void shouldReturn404ForNonExistentCustomer() throws Exception {
        mockMvc.perform(post("/api/customers/999/credit-check")
                        .with(httpBasic("admin", "admin123")))
                .andExpect(status().isNotFound());
    }
}
