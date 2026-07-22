package com.redhat.lightwell.controller;

import java.util.List;
import javax.validation.Valid;
import com.redhat.lightwell.model.dto.AccountResponse;
import com.redhat.lightwell.model.dto.CreateCustomerRequest;
import com.redhat.lightwell.model.dto.CustomerResponse;
import com.redhat.lightwell.model.dto.UpdateCustomerRequest;
import com.redhat.lightwell.service.AccountService;
import com.redhat.lightwell.service.CustomerService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    private final CustomerService customerService;
    private final AccountService accountService;

    public CustomerController(CustomerService customerService, AccountService accountService) {
        this.customerService = customerService;
        this.accountService = accountService;
    }

    @GetMapping
    public List<CustomerResponse> getAll() {
        return customerService.findAll();
    }

    @GetMapping("/{id}")
    public CustomerResponse getById(@PathVariable Long id) {
        return customerService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CustomerResponse create(@Valid @RequestBody CreateCustomerRequest request) {
        return customerService.create(request);
    }

    @PutMapping("/{id}")
    public CustomerResponse update(@PathVariable Long id,
                                   @Valid @RequestBody UpdateCustomerRequest request) {
        return customerService.update(id, request);
    }

    @GetMapping("/{customerId}/accounts")
    public List<AccountResponse> getCustomerAccounts(@PathVariable Long customerId) {
        return accountService.findByCustomerId(customerId);
    }
}
