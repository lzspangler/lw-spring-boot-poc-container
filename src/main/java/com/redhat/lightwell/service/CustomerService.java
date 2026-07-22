package com.redhat.lightwell.service;

import java.util.List;
import java.util.stream.Collectors;
import com.redhat.lightwell.exception.DuplicateResourceException;
import com.redhat.lightwell.exception.ResourceNotFoundException;
import com.redhat.lightwell.model.Customer;
import com.redhat.lightwell.model.dto.CreateCustomerRequest;
import com.redhat.lightwell.model.dto.CustomerResponse;
import com.redhat.lightwell.model.dto.UpdateCustomerRequest;
import com.redhat.lightwell.repository.CustomerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class CustomerService {

    private static final Logger log = LoggerFactory.getLogger(CustomerService.class);

    private final CustomerRepository customerRepository;

    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    public List<CustomerResponse> findAll() {
        log.info("Retrieving all customers");
        List<CustomerResponse> customers = customerRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
        log.info("Found {} customers", customers.size());
        return customers;
    }

    public CustomerResponse findById(Long id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", id));
        return toResponse(customer);
    }

    @Transactional
    public CustomerResponse create(CreateCustomerRequest request) {
        log.info("Creating customer: {} {} with email {}", request.getFirstName(),
                request.getLastName(), request.getEmail());
        if (customerRepository.existsByEmail(request.getEmail())) {
            log.warn("Duplicate email detected: {}", request.getEmail());
            throw new DuplicateResourceException("Customer", "email", request.getEmail());
        }
        Customer customer = new Customer(
                request.getFirstName(),
                request.getLastName(),
                request.getEmail(),
                request.getPhoneNumber());
        customer = customerRepository.save(customer);
        log.info("Customer created with id {}: {} {}", customer.getId(),
                customer.getFirstName(), customer.getLastName());
        return toResponse(customer);
    }

    @Transactional
    public CustomerResponse update(Long id, UpdateCustomerRequest request) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", id));

        if (StringUtils.hasText(request.getFirstName())) {
            customer.setFirstName(request.getFirstName());
        }
        if (StringUtils.hasText(request.getLastName())) {
            customer.setLastName(request.getLastName());
        }
        if (StringUtils.hasText(request.getEmail())) {
            if (!request.getEmail().equals(customer.getEmail())
                    && customerRepository.existsByEmail(request.getEmail())) {
                throw new DuplicateResourceException("Customer", "email", request.getEmail());
            }
            customer.setEmail(request.getEmail());
        }
        if (StringUtils.hasText(request.getPhoneNumber())) {
            customer.setPhoneNumber(request.getPhoneNumber());
        }

        customer = customerRepository.save(customer);
        return toResponse(customer);
    }

    private CustomerResponse toResponse(Customer customer) {
        return new CustomerResponse(
                customer.getId(),
                customer.getFirstName(),
                customer.getLastName(),
                customer.getEmail(),
                customer.getPhoneNumber(),
                customer.getCreatedAt());
    }
}
