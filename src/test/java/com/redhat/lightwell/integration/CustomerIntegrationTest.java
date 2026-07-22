package com.redhat.lightwell.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class CustomerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldGetAllSeededCustomers() throws Exception {
        mockMvc.perform(get("/api/customers")
                        .with(httpBasic("admin", "admin123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    void shouldGetCustomerById() throws Exception {
        mockMvc.perform(get("/api/customers/1")
                        .with(httpBasic("admin", "admin123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"))
                .andExpect(jsonPath("$.email").value("john.doe@example.com"))
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void shouldReturn404ForNonExistentCustomer() throws Exception {
        mockMvc.perform(get("/api/customers/999")
                        .with(httpBasic("admin", "admin123")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void shouldCreateCustomer() throws Exception {
        mockMvc.perform(post("/api/customers")
                        .with(httpBasic("admin", "admin123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"firstName\":\"Alice\",\"lastName\":\"Wonder\","
                                + "\"email\":\"alice@example.com\",\"phoneNumber\":\"555-0103\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.firstName").value("Alice"))
                .andExpect(jsonPath("$.lastName").value("Wonder"))
                .andExpect(jsonPath("$.email").value("alice@example.com"))
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.createdAt").isNotEmpty());
    }

    @Test
    void shouldReturn400WhenCreateWithMissingFields() throws Exception {
        mockMvc.perform(post("/api/customers")
                        .with(httpBasic("admin", "admin123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"firstName\":\"\",\"lastName\":\"\","
                                + "\"email\":\"\",\"phoneNumber\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors").isArray())
                .andExpect(jsonPath("$.fieldErrors", hasSize(greaterThanOrEqualTo(3))));
    }

    @Test
    void shouldReturn400WhenCreateWithInvalidEmail() throws Exception {
        mockMvc.perform(post("/api/customers")
                        .with(httpBasic("admin", "admin123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"firstName\":\"Alice\",\"lastName\":\"Wonder\","
                                + "\"email\":\"not-an-email\",\"phoneNumber\":\"555-0103\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn409WhenCreateWithDuplicateEmail() throws Exception {
        mockMvc.perform(post("/api/customers")
                        .with(httpBasic("admin", "admin123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"firstName\":\"Alice\",\"lastName\":\"Wonder\","
                                + "\"email\":\"john.doe@example.com\",\"phoneNumber\":\"555-0103\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void shouldUpdateCustomer() throws Exception {
        mockMvc.perform(put("/api/customers/1")
                        .with(httpBasic("admin", "admin123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"firstName\":\"Jonathan\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Jonathan"))
                .andExpect(jsonPath("$.lastName").value("Doe"));
    }

    @Test
    void shouldReturn404WhenUpdateNonExistentCustomer() throws Exception {
        mockMvc.perform(put("/api/customers/999")
                        .with(httpBasic("admin", "admin123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"firstName\":\"Test\"}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturn409WhenUpdateEmailToDuplicate() throws Exception {
        mockMvc.perform(put("/api/customers/1")
                        .with(httpBasic("admin", "admin123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"jane.smith@example.com\"}"))
                .andExpect(status().isConflict());
    }

    @Test
    void shouldGetCustomerAccounts() throws Exception {
        mockMvc.perform(get("/api/customers/1/accounts")
                        .with(httpBasic("admin", "admin123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    void shouldReturn401WhenNotAuthenticated() throws Exception {
        mockMvc.perform(get("/api/customers"))
                .andExpect(status().isUnauthorized());
    }
}
