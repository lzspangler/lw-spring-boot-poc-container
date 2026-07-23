package com.redhat.lightwell.service;

import java.util.Optional;
import com.redhat.lightwell.exception.DuplicateResourceException;
import com.redhat.lightwell.exception.ResourceNotFoundException;
import com.redhat.lightwell.model.Customer;
import com.redhat.lightwell.model.dto.CreateCustomerRequest;
import com.redhat.lightwell.model.dto.CustomerResponse;
import com.redhat.lightwell.model.dto.UpdateCustomerRequest;
import com.redhat.lightwell.repository.CustomerRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private CustomerService customerService;

    @Test
    void shouldCreateCustomerSuccessfully() {
        CreateCustomerRequest request = new CreateCustomerRequest(
                "John", "Doe", "john@example.com", "555-0101");
        Customer saved = new Customer("John", "Doe", "john@example.com", "555-0101");

        when(customerRepository.existsByEmail("john@example.com")).thenReturn(false);
        when(customerRepository.save(any(Customer.class))).thenReturn(saved);

        CustomerResponse response = customerService.create(request);

        assertThat(response.getFirstName()).isEqualTo("John");
        assertThat(response.getLastName()).isEqualTo("Doe");
        assertThat(response.getEmail()).isEqualTo("john@example.com");
    }

    @Test
    void shouldThrowDuplicateWhenEmailExists() {
        CreateCustomerRequest request = new CreateCustomerRequest(
                "John", "Doe", "john@example.com", "555-0101");

        when(customerRepository.existsByEmail("john@example.com")).thenReturn(true);

        assertThatThrownBy(() -> customerService.create(request))
                .isInstanceOf(DuplicateResourceException.class);
    }

    @Test
    void shouldFindCustomerById() {
        Customer customer = new Customer("Jane", "Smith", "jane@example.com", "555-0102");
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));

        CustomerResponse response = customerService.findById(1L);

        assertThat(response.getFirstName()).isEqualTo("Jane");
    }

    @Test
    void shouldThrowNotFoundWhenCustomerMissing() {
        when(customerRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> customerService.findById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void shouldUpdateAllFields() {
        Customer customer = new Customer("John", "Doe", "john@example.com", "555-0101");
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(customerRepository.existsByEmail("newemail@example.com")).thenReturn(false);
        when(customerRepository.save(any(Customer.class))).thenReturn(customer);

        UpdateCustomerRequest request = new UpdateCustomerRequest();
        request.setFirstName("Jonathan");
        request.setLastName("Smith");
        request.setEmail("newemail@example.com");
        request.setPhoneNumber("555-9999");

        CustomerResponse response = customerService.update(1L, request);

        assertThat(response.getFirstName()).isEqualTo("Jonathan");
        assertThat(response.getLastName()).isEqualTo("Smith");
    }

    @Test
    void shouldUpdateWithSameEmailWithoutDuplicateCheck() {
        Customer customer = new Customer("John", "Doe", "john@example.com", "555-0101");
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(customerRepository.save(any(Customer.class))).thenReturn(customer);

        UpdateCustomerRequest request = new UpdateCustomerRequest();
        request.setEmail("john@example.com");

        CustomerResponse response = customerService.update(1L, request);

        assertThat(response.getEmail()).isEqualTo("john@example.com");
    }
}
