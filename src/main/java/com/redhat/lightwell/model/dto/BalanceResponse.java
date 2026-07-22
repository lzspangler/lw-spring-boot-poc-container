package com.redhat.lightwell.model.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class BalanceResponse {

    private final Long accountId;
    private final String accountNumber;
    private final BigDecimal balance;
    private final LocalDateTime asOf;

    public BalanceResponse(Long accountId, String accountNumber, BigDecimal balance, LocalDateTime asOf) {
        this.accountId = accountId;
        this.accountNumber = accountNumber;
        this.balance = balance;
        this.asOf = asOf;
    }

    public Long getAccountId() {
        return accountId;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public LocalDateTime getAsOf() {
        return asOf;
    }
}
