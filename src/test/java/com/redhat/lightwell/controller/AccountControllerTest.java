package com.redhat.lightwell.controller;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import com.redhat.lightwell.model.AccountType;
import com.redhat.lightwell.model.TransactionType;
import com.redhat.lightwell.model.dto.AccountResponse;
import com.redhat.lightwell.model.dto.BalanceResponse;
import com.redhat.lightwell.model.dto.TransactionResponse;
import com.redhat.lightwell.service.AccountService;
import com.redhat.lightwell.service.TransactionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AccountController.class)
@WithMockUser
class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AccountService accountService;

    @MockBean
    private TransactionService transactionService;

    @Test
    void shouldReturnAccount() throws Exception {
        AccountResponse account = new AccountResponse(
                1L, "ACC-000001", AccountType.CHECKING, new BigDecimal("1000.00"),
                1L, "John Doe", LocalDateTime.now());
        when(accountService.findById(1L)).thenReturn(account);

        mockMvc.perform(get("/api/accounts/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountNumber").value("ACC-000001"));
    }

    @Test
    void shouldReturnBalance() throws Exception {
        BalanceResponse balance = new BalanceResponse(
                1L, "ACC-000001", new BigDecimal("2500.00"), LocalDateTime.now());
        when(accountService.getBalance(1L)).thenReturn(balance);

        mockMvc.perform(get("/api/accounts/1/balance"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(2500.00));
    }

    @Test
    void shouldDeposit() throws Exception {
        TransactionResponse txn = new TransactionResponse(
                1L, TransactionType.DEPOSIT, new BigDecimal("500.00"), "Test deposit",
                new BigDecimal("1500.00"), 1L, "ACC-000001", null, LocalDateTime.now());
        when(transactionService.deposit(eq(1L), any())).thenReturn(txn);

        mockMvc.perform(post("/api/accounts/1/deposit")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amount\":500.00,\"description\":\"Test deposit\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balanceAfter").value(1500.00));
    }

    @Test
    void shouldCreateAccount() throws Exception {
        AccountResponse account = new AccountResponse(
                1L, "ACC-000001", AccountType.CHECKING, BigDecimal.ZERO,
                1L, "John Doe", LocalDateTime.now());
        when(accountService.create(any())).thenReturn(account);

        mockMvc.perform(post("/api/accounts")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"customerId\":1,\"accountType\":\"CHECKING\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accountNumber").value("ACC-000001"));
    }
}
