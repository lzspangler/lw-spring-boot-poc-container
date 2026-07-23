package com.redhat.lightwell.service;

import java.util.Optional;
import com.redhat.lightwell.exception.ResourceNotFoundException;
import com.redhat.lightwell.model.Customer;
import com.redhat.lightwell.model.dto.CreditCheckResponse;
import com.redhat.lightwell.repository.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreditCheckServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private AuditService auditService;

    private CreditCheckService creditCheckService;

    @BeforeEach
    void setUp() {
        creditCheckService = new CreditCheckService(customerRepository, auditService,
                "http://localhost:19999/nonexistent");
    }

    @Test
    void shouldThrowNotFoundWhenCustomerMissing() {
        when(customerRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> creditCheckService.performCreditCheck(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void shouldReturnFallbackScoreWhenHttpCallFails() {
        Customer customer = new Customer("John", "Doe", "john@example.com", "555-0101");
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));

        CreditCheckResponse response = creditCheckService.performCreditCheck(1L);

        assertThat(response.getCustomerId()).isEqualTo(1L);
        assertThat(response.getScore()).isBetween(600, 799);
        assertThat(response.getProvider()).isEqualTo("ExternalCreditBureau");
        assertThat(response.getCheckedAt()).isNotNull();
    }

    @Test
    void shouldReturnDeterministicFallbackScore() {
        Customer customer = new Customer("Jane", "Smith", "jane@example.com", "555-0102");
        when(customerRepository.findById(2L)).thenReturn(Optional.of(customer));

        CreditCheckResponse first = creditCheckService.performCreditCheck(2L);
        CreditCheckResponse second = creditCheckService.performCreditCheck(2L);

        assertThat(first.getScore()).isEqualTo(second.getScore());
        assertThat(first.getStatus()).isEqualTo(second.getStatus());
    }

    @Test
    void shouldMapScoreToGoodStatus() {
        // hashCode("high-score@test.com") % 200 needs to produce >= 100 for score >= 700
        // We try emails until we find one that maps to GOOD
        Customer customer = createCustomerWithExpectedStatus("GOOD");
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));

        CreditCheckResponse response = creditCheckService.performCreditCheck(1L);

        assertThat(response.getStatus()).isEqualTo("GOOD");
        assertThat(response.getScore()).isGreaterThanOrEqualTo(700);
    }

    @Test
    void shouldMapScoreToFairStatus() {
        Customer customer = createCustomerWithExpectedStatus("FAIR");
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));

        CreditCheckResponse response = creditCheckService.performCreditCheck(1L);

        assertThat(response.getStatus()).isEqualTo("FAIR");
        assertThat(response.getScore()).isBetween(500, 699);
    }

    @Test
    void shouldReturnValidStatusForAnyCustomer() {
        Customer customer = new Customer("Any", "User", "any@example.com", "555-0199");
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));

        CreditCheckResponse response = creditCheckService.performCreditCheck(1L);

        assertThat(response.getStatus()).isIn("GOOD", "FAIR");
        assertThat(response.getScore()).isBetween(600, 799);
    }

    private Customer createCustomerWithExpectedStatus(String expectedStatus) {
        // Brute-force find an email whose hashCode % 200 lands in the right bracket
        for (int i = 0; i < 1000; i++) {
            String email = "user" + i + "@test.com";
            int score = 600 + Math.abs(email.hashCode() % 200);
            String status = score >= 700 ? "GOOD" : score >= 500 ? "FAIR" : "POOR";
            if (status.equals(expectedStatus)) {
                return new Customer("Test", "User", email, "555-0000");
            }
        }
        throw new IllegalStateException("Could not find email producing status: " + expectedStatus);
    }
}
