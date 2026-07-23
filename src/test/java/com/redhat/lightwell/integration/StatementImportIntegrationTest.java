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
class StatementImportIntegrationTest {

    private static final String XML_STATEMENT = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
            + "<statement>"
            + "<transaction><type>DEPOSIT</type><amount>1000.00</amount>"
            + "<description>Wire transfer in</description></transaction>"
            + "<transaction><type>WITHDRAWAL</type><amount>250.00</amount>"
            + "<description>Bill payment</description></transaction>"
            + "</statement>";

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldImportXmlStatement() throws Exception {
        mockMvc.perform(post("/api/accounts/1/import-statement")
                        .with(httpBasic("admin", "admin123"))
                        .contentType(MediaType.APPLICATION_XML)
                        .content(XML_STATEMENT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountId").value(1))
                .andExpect(jsonPath("$.transactionsImported").value(2))
                .andExpect(jsonPath("$.status").value("SUCCESS"));
    }

    @Test
    void shouldVerifyTransactionsCreatedAfterImport() throws Exception {
        mockMvc.perform(post("/api/accounts/1/import-statement")
                        .with(httpBasic("admin", "admin123"))
                        .contentType(MediaType.APPLICATION_XML)
                        .content(XML_STATEMENT))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/accounts/1/transactions")
                        .with(httpBasic("admin", "admin123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(4)));
    }

    @Test
    void shouldUpdateAccountBalanceAfterImport() throws Exception {
        mockMvc.perform(post("/api/accounts/1/import-statement")
                        .with(httpBasic("admin", "admin123"))
                        .contentType(MediaType.APPLICATION_XML)
                        .content(XML_STATEMENT))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/accounts/1/balance")
                        .with(httpBasic("admin", "admin123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(3250.00));
    }

    @Test
    void shouldReturn500ForMalformedXml() throws Exception {
        mockMvc.perform(post("/api/accounts/1/import-statement")
                        .with(httpBasic("admin", "admin123"))
                        .contentType(MediaType.APPLICATION_XML)
                        .content("<<<not valid xml>>>"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void shouldReturn404ForNonExistentAccount() throws Exception {
        mockMvc.perform(post("/api/accounts/999/import-statement")
                        .with(httpBasic("admin", "admin123"))
                        .contentType(MediaType.APPLICATION_XML)
                        .content(XML_STATEMENT))
                .andExpect(status().isNotFound());
    }
}
