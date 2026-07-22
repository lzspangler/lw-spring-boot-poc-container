package com.redhat.lightwell.regression;

import java.nio.charset.StandardCharsets;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class BankingWorkflowRegressionTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldCompleteFullCustomerOnboardingWorkflow() throws Exception {
        String customerBody = mockMvc.perform(post("/api/customers")
                        .with(httpBasic("admin", "admin123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"firstName\":\"Alice\",\"lastName\":\"Wonder\","
                                + "\"email\":\"alice@example.com\",\"phoneNumber\":\"555-0200\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        long customerId = objectMapper.readTree(customerBody).get("id").asLong();

        String accountBody = mockMvc.perform(post("/api/accounts")
                        .with(httpBasic("admin", "admin123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"customerId\":" + customerId + ",\"accountType\":\"CHECKING\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.balance").value(0))
                .andReturn().getResponse().getContentAsString();
        long accountId = objectMapper.readTree(accountBody).get("id").asLong();

        mockMvc.perform(post("/api/accounts/" + accountId + "/deposit")
                        .with(httpBasic("admin", "admin123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amount\":1000.00,\"description\":\"Initial deposit\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balanceAfter").value(1000.00));

        mockMvc.perform(post("/api/accounts/" + accountId + "/withdraw")
                        .with(httpBasic("admin", "admin123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amount\":200.00,\"description\":\"ATM withdrawal\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balanceAfter").value(800.00));

        mockMvc.perform(get("/api/accounts/" + accountId + "/balance")
                        .with(httpBasic("admin", "admin123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(800.00));

        mockMvc.perform(get("/api/accounts/" + accountId + "/transactions")
                        .with(httpBasic("admin", "admin123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));

        mockMvc.perform(get("/api/customers/" + customerId + "/accounts")
                        .with(httpBasic("admin", "admin123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void shouldCompleteFullTransferWorkflow() throws Exception {
        mockMvc.perform(get("/api/accounts/1/balance")
                        .with(httpBasic("admin", "admin123")))
                .andExpect(jsonPath("$.balance").value(2500.00));

        mockMvc.perform(get("/api/accounts/3/balance")
                        .with(httpBasic("admin", "admin123")))
                .andExpect(jsonPath("$.balance").value(5000.00));

        mockMvc.perform(post("/api/transfers")
                        .with(httpBasic("admin", "admin123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sourceAccountId\":1,\"targetAccountId\":3,"
                                + "\"amount\":1000.00,\"description\":\"Rent payment\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.balanceAfter").value(1500.00));

        mockMvc.perform(get("/api/accounts/1/balance")
                        .with(httpBasic("admin", "admin123")))
                .andExpect(jsonPath("$.balance").value(1500.00));

        mockMvc.perform(get("/api/accounts/3/balance")
                        .with(httpBasic("admin", "admin123")))
                .andExpect(jsonPath("$.balance").value(6000.00));

        mockMvc.perform(get("/api/accounts/1/transactions")
                        .with(httpBasic("admin", "admin123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)));

        mockMvc.perform(get("/api/accounts/3/transactions")
                        .with(httpBasic("admin", "admin123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void shouldCompleteDocumentAndStatementWorkflow() throws Exception {
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
                .andExpect(jsonPath("$", hasSize(1)));

        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                + "<statement>"
                + "<transaction><type>DEPOSIT</type><amount>1000.00</amount>"
                + "<description>Wire in</description></transaction>"
                + "<transaction><type>DEPOSIT</type><amount>500.00</amount>"
                + "<description>Refund</description></transaction>"
                + "</statement>";

        mockMvc.perform(post("/api/accounts/1/import-statement")
                        .with(httpBasic("admin", "admin123"))
                        .contentType(MediaType.APPLICATION_XML)
                        .content(xml))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactionsImported").value(2));

        mockMvc.perform(get("/api/accounts/1/balance")
                        .with(httpBasic("admin", "admin123")))
                .andExpect(jsonPath("$.balance").value(4000.00));

        mockMvc.perform(get("/api/accounts/1/transactions")
                        .with(httpBasic("admin", "admin123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(4)));
    }

    @Test
    void shouldHandleInsufficientFundsAndRetryAfterDeposit() throws Exception {
        mockMvc.perform(post("/api/accounts/3/withdraw")
                        .with(httpBasic("admin", "admin123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amount\":99999.00,\"description\":\"Too much\"}"))
                .andExpect(status().isUnprocessableEntity());

        mockMvc.perform(get("/api/accounts/3/balance")
                        .with(httpBasic("admin", "admin123")))
                .andExpect(jsonPath("$.balance").value(5000.00));

        mockMvc.perform(post("/api/accounts/3/deposit")
                        .with(httpBasic("admin", "admin123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amount\":100000.00,\"description\":\"Large deposit\"}"))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/accounts/3/withdraw")
                        .with(httpBasic("admin", "admin123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amount\":99999.00,\"description\":\"Large withdrawal\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balanceAfter").value(5001.00));

        mockMvc.perform(get("/api/accounts/3/balance")
                        .with(httpBasic("admin", "admin123")))
                .andExpect(jsonPath("$.balance").value(5001.00));
    }

    @Test
    void shouldCompleteMultiAccountCustomerWorkflow() throws Exception {
        String accountBody = mockMvc.perform(post("/api/accounts")
                        .with(httpBasic("admin", "admin123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"customerId\":2,\"accountType\":\"SAVINGS\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        long newAccountId = objectMapper.readTree(accountBody).get("id").asLong();

        mockMvc.perform(post("/api/accounts/" + newAccountId + "/deposit")
                        .with(httpBasic("admin", "admin123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amount\":3000.00,\"description\":\"Initial savings\"}"))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/transfers")
                        .with(httpBasic("admin", "admin123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sourceAccountId\":3,\"targetAccountId\":" + newAccountId + ","
                                + "\"amount\":2000.00,\"description\":\"To savings\"}"))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/customers/2/accounts")
                        .with(httpBasic("admin", "admin123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));

        mockMvc.perform(get("/api/accounts/3/balance")
                        .with(httpBasic("admin", "admin123")))
                .andExpect(jsonPath("$.balance").value(3000.00));

        mockMvc.perform(get("/api/accounts/" + newAccountId + "/balance")
                        .with(httpBasic("admin", "admin123")))
                .andExpect(jsonPath("$.balance").value(5000.00));
    }
}
