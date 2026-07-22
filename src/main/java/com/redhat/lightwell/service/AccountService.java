package com.redhat.lightwell.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import com.redhat.lightwell.exception.ResourceNotFoundException;
import com.redhat.lightwell.model.Account;
import com.redhat.lightwell.model.Customer;
import com.redhat.lightwell.model.dto.AccountResponse;
import com.redhat.lightwell.model.dto.BalanceResponse;
import com.redhat.lightwell.model.dto.CreateAccountRequest;
import com.redhat.lightwell.repository.AccountRepository;
import com.redhat.lightwell.repository.CustomerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AccountService {

    private static final Logger log = LoggerFactory.getLogger(AccountService.class);

    private final AccountRepository accountRepository;
    private final CustomerRepository customerRepository;
    private final AuditService auditService;

    public AccountService(AccountRepository accountRepository, CustomerRepository customerRepository,
                          AuditService auditService) {
        this.accountRepository = accountRepository;
        this.customerRepository = customerRepository;
        this.auditService = auditService;
    }

    public AccountResponse findById(Long id) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Account", id));
        return toResponse(account);
    }

    public List<AccountResponse> findByCustomerId(Long customerId) {
        if (!customerRepository.existsById(customerId)) {
            throw new ResourceNotFoundException("Customer", customerId);
        }
        return accountRepository.findByCustomerId(customerId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public AccountResponse create(CreateAccountRequest request) {
        log.info("Creating {} account for customer {}", request.getAccountType(), request.getCustomerId());
        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer", request.getCustomerId()));

        String accountNumber = generateAccountNumber();
        Account account = new Account(accountNumber, request.getAccountType(), BigDecimal.ZERO, customer);
        account = accountRepository.save(account);

        log.info("Account {} created for customer {} {}", accountNumber,
                customer.getFirstName(), customer.getLastName());

        auditService.log("ACCOUNT_CREATED", "Account", account.getId(),
                "Created " + request.getAccountType() + " account " + accountNumber
                        + " for customer " + customer.getId());

        return toResponse(account);
    }

    public BalanceResponse getBalance(Long accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account", accountId));
        return new BalanceResponse(account.getId(), account.getAccountNumber(),
                account.getBalance(), LocalDateTime.now());
    }

    private String generateAccountNumber() {
        long count = accountRepository.count();
        return String.format("ACC-%06d", count + 1);
    }

    private AccountResponse toResponse(Account account) {
        Customer customer = account.getCustomer();
        return new AccountResponse(
                account.getId(),
                account.getAccountNumber(),
                account.getAccountType(),
                account.getBalance(),
                customer.getId(),
                customer.getFirstName() + " " + customer.getLastName(),
                account.getCreatedAt());
    }
}
