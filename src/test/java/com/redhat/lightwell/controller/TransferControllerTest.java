package com.redhat.lightwell.controller;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import com.redhat.lightwell.model.TransactionType;
import com.redhat.lightwell.model.dto.TransactionResponse;
import com.redhat.lightwell.service.TransactionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TransferController.class)
@WithMockUser
class TransferControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TransactionService transactionService;

    @Test
    void shouldTransferSuccessfully() throws Exception {
        TransactionResponse txn = new TransactionResponse(
                1L, TransactionType.TRANSFER, new BigDecimal("200.00"), "Transfer",
                new BigDecimal("800.00"), 1L, "ACC-000001", 2L, LocalDateTime.now());
        when(transactionService.transfer(any())).thenReturn(txn);

        mockMvc.perform(post("/api/transfers")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sourceAccountId\":1,\"targetAccountId\":2,"
                                + "\"amount\":200.00,\"description\":\"Transfer\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.balanceAfter").value(800.00));
    }

    @Test
    void shouldReturn400WhenMissingFields() throws Exception {
        mockMvc.perform(post("/api/transfers")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amount\":100.00}"))
                .andExpect(status().isBadRequest());
    }
}
