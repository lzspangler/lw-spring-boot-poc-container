package com.redhat.lightwell.model.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import com.redhat.lightwell.model.TransactionType;

public class TransactionResponse {

    private final Long id;
    private final TransactionType transactionType;
    private final BigDecimal amount;
    private final String description;
    private final BigDecimal balanceAfter;
    private final Long accountId;
    private final String accountNumber;
    private final Long relatedAccountId;
    private final LocalDateTime createdAt;

    public TransactionResponse(Long id, TransactionType transactionType, BigDecimal amount, String description,
                               BigDecimal balanceAfter, Long accountId, String accountNumber,
                               Long relatedAccountId, LocalDateTime createdAt) {
        this.id = id;
        this.transactionType = transactionType;
        this.amount = amount;
        this.description = description;
        this.balanceAfter = balanceAfter;
        this.accountId = accountId;
        this.accountNumber = accountNumber;
        this.relatedAccountId = relatedAccountId;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public TransactionType getTransactionType() {
        return transactionType;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getDescription() {
        return description;
    }

    public BigDecimal getBalanceAfter() {
        return balanceAfter;
    }

    public Long getAccountId() {
        return accountId;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public Long getRelatedAccountId() {
        return relatedAccountId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
