package com.redhat.lightwell.model.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import com.redhat.lightwell.model.AccountType;

public class AccountResponse {

    private final Long id;
    private final String accountNumber;
    private final AccountType accountType;
    private final BigDecimal balance;
    private final Long customerId;
    private final String customerName;
    private final LocalDateTime createdAt;

    public AccountResponse(Long id, String accountNumber, AccountType accountType, BigDecimal balance,
                           Long customerId, String customerName, LocalDateTime createdAt) {
        this.id = id;
        this.accountNumber = accountNumber;
        this.accountType = accountType;
        this.balance = balance;
        this.customerId = customerId;
        this.customerName = customerName;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public AccountType getAccountType() {
        return accountType;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
