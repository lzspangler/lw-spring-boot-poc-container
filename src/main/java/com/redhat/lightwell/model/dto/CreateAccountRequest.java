package com.redhat.lightwell.model.dto;

import javax.validation.constraints.NotNull;
import com.redhat.lightwell.model.AccountType;

public class CreateAccountRequest {

    @NotNull(message = "Customer ID is required")
    private Long customerId;

    @NotNull(message = "Account type is required")
    private AccountType accountType;

    public CreateAccountRequest() {
    }

    public CreateAccountRequest(Long customerId, AccountType accountType) {
        this.customerId = customerId;
        this.accountType = accountType;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public AccountType getAccountType() {
        return accountType;
    }

    public void setAccountType(AccountType accountType) {
        this.accountType = accountType;
    }
}
