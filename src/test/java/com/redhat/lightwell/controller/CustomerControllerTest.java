package com.redhat.lightwell.controller;

import java.time.LocalDateTime;
import java.util.List;
import com.redhat.lightwell.exception.ResourceNotFoundException;
import com.redhat.lightwell.model.dto.CustomerResponse;
import com.redhat.lightwell.service.AccountService;
import com.redhat.lightwell.service.CustomerService;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CustomerController.class)
@WithMockUser
class CustomerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CustomerService customerService;

    @MockBean
    private AccountService accountService;

    @Test
    void shouldReturnAllCustomers() throws Exception {
        CustomerResponse customer = new CustomerResponse(
                1L, "John", "Doe", "john@example.com", "555-0101", LocalDateTime.now());
        when(customerService.findAll()).thenReturn(List.of(customer));

        mockMvc.perform(get("/api/customers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].firstName").value("John"));
    }

    @Test
    void shouldReturnCustomerById() throws Exception {
        CustomerResponse customer = new CustomerResponse(
                1L, "John", "Doe", "john@example.com", "555-0101", LocalDateTime.now());
        when(customerService.findById(1L)).thenReturn(customer);

        mockMvc.perform(get("/api/customers/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("john@example.com"));
    }

    @Test
    void shouldReturn404WhenCustomerNotFound() throws Exception {
        when(customerService.findById(99L)).thenThrow(new ResourceNotFoundException("Customer", 99L));

        mockMvc.perform(get("/api/customers/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldCreateCustomer() throws Exception {
        CustomerResponse customer = new CustomerResponse(
                1L, "John", "Doe", "john@example.com", "555-0101", LocalDateTime.now());
        when(customerService.create(any())).thenReturn(customer);

        mockMvc.perform(post("/api/customers")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"firstName\":\"John\",\"lastName\":\"Doe\","
                                + "\"email\":\"john@example.com\",\"phoneNumber\":\"555-0101\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.firstName").value("John"));
    }

    @Test
    void shouldReturn400WhenValidationFails() throws Exception {
        mockMvc.perform(post("/api/customers")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"firstName\":\"\",\"lastName\":\"\","
                                + "\"email\":\"not-an-email\",\"phoneNumber\":\"\"}"))
                .andExpect(status().isBadRequest());
    }
}
