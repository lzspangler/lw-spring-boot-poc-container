package com.redhat.lightwell.exception;

import java.math.BigDecimal;

public class InsufficientFundsException extends RuntimeException {

    private final Long accountId;
    private final BigDecimal requested;
    private final BigDecimal available;

    public InsufficientFundsException(Long accountId, BigDecimal requested, BigDecimal available) {
        super("Insufficient funds in account " + accountId
                + ": requested " + requested + ", available " + available);
        this.accountId = accountId;
        this.requested = requested;
        this.available = available;
    }

    public Long getAccountId() {
        return accountId;
    }

    public BigDecimal getRequested() {
        return requested;
    }

    public BigDecimal getAvailable() {
        return available;
    }
}
