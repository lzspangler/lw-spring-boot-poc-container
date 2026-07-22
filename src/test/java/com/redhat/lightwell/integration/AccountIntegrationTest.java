package com.redhat.lightwell.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AccountIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldGetAccountById() throws Exception {
        mockMvc.perform(get("/api/accounts/1")
                        .with(httpBasic("admin", "admin123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountNumber").value("ACC-000001"))
                .andExpect(jsonPath("$.accountType").value("CHECKING"))
                .andExpect(jsonPath("$.balance").value(2500.00))
                .andExpect(jsonPath("$.customerName").value("John Doe"));
    }

    @Test
    void shouldReturn404ForNonExistentAccount() throws Exception {
        mockMvc.perform(get("/api/accounts/999")
                        .with(httpBasic("admin", "admin123")))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldCreateAccount() throws Exception {
        mockMvc.perform(post("/api/accounts")
                        .with(httpBasic("admin", "admin123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"customerId\":2,\"accountType\":\"SAVINGS\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accountType").value("SAVINGS"))
                .andExpect(jsonPath("$.balance").value(0))
                .andExpect(jsonPath("$.accountNumber").isString());
    }

    @Test
    void shouldReturn404WhenCreateAccountForNonExistentCustomer() throws Exception {
        mockMvc.perform(post("/api/accounts")
                        .with(httpBasic("admin", "admin123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"customerId\":999,\"accountType\":\"CHECKING\"}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturn400WhenCreateAccountWithMissingFields() throws Exception {
        mockMvc.perform(post("/api/accounts")
                        .with(httpBasic("admin", "admin123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldGetBalance() throws Exception {
        mockMvc.perform(get("/api/accounts/1/balance")
                        .with(httpBasic("admin", "admin123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountNumber").value("ACC-000001"))
                .andExpect(jsonPath("$.balance").value(2500.00));
    }

    @Test
    void shouldDeposit() throws Exception {
        mockMvc.perform(post("/api/accounts/1/deposit")
                        .with(httpBasic("admin", "admin123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amount\":500.00,\"description\":\"Test deposit\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactionType").value("DEPOSIT"))
                .andExpect(jsonPath("$.amount").value(500.00))
                .andExpect(jsonPath("$.balanceAfter").value(3000.00));
    }

    @Test
    void shouldWithdraw() throws Exception {
        mockMvc.perform(post("/api/accounts/1/withdraw")
                        .with(httpBasic("admin", "admin123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amount\":200.00,\"description\":\"ATM withdrawal\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactionType").value("WITHDRAWAL"))
                .andExpect(jsonPath("$.balanceAfter").value(2300.00));
    }

    @Test
    void shouldReturn422WhenWithdrawInsufficientFunds() throws Exception {
        mockMvc.perform(post("/api/accounts/1/withdraw")
                        .with(httpBasic("admin", "admin123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amount\":99999.00,\"description\":\"Too much\"}"))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void shouldGetTransactionHistory() throws Exception {
        mockMvc.perform(get("/api/accounts/1/transactions")
                        .with(httpBasic("admin", "admin123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }
}
