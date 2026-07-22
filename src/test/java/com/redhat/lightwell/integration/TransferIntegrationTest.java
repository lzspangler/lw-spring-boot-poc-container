package com.redhat.lightwell.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class TransferIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldTransferBetweenAccounts() throws Exception {
        mockMvc.perform(post("/api/transfers")
                        .with(httpBasic("admin", "admin123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sourceAccountId\":1,\"targetAccountId\":3,"
                                + "\"amount\":500.00,\"description\":\"Test transfer\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.transactionType").value("TRANSFER"))
                .andExpect(jsonPath("$.amount").value(500.00))
                .andExpect(jsonPath("$.balanceAfter").value(2000.00));

        mockMvc.perform(get("/api/accounts/3/balance")
                        .with(httpBasic("admin", "admin123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(5500.00));
    }

    @Test
    void shouldReturn400WhenSelfTransfer() throws Exception {
        mockMvc.perform(post("/api/transfers")
                        .with(httpBasic("admin", "admin123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sourceAccountId\":1,\"targetAccountId\":1,\"amount\":100.00}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn422WhenTransferInsufficientFunds() throws Exception {
        mockMvc.perform(post("/api/transfers")
                        .with(httpBasic("admin", "admin123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sourceAccountId\":1,\"targetAccountId\":3,\"amount\":99999.00}"))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void shouldReturn404WhenSourceAccountNotFound() throws Exception {
        mockMvc.perform(post("/api/transfers")
                        .with(httpBasic("admin", "admin123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sourceAccountId\":999,\"targetAccountId\":1,\"amount\":100.00}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturn404WhenTargetAccountNotFound() throws Exception {
        mockMvc.perform(post("/api/transfers")
                        .with(httpBasic("admin", "admin123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sourceAccountId\":1,\"targetAccountId\":999,\"amount\":100.00}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturn400WhenMissingRequiredFields() throws Exception {
        mockMvc.perform(post("/api/transfers")
                        .with(httpBasic("admin", "admin123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amount\":100.00}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors", hasSize(greaterThanOrEqualTo(1))));
    }
}
