package com.redhat.lightwell.controller;

import java.util.List;
import javax.validation.Valid;
import com.redhat.lightwell.model.dto.AccountResponse;
import com.redhat.lightwell.model.dto.BalanceResponse;
import com.redhat.lightwell.model.dto.CreateAccountRequest;
import com.redhat.lightwell.model.dto.TransactionRequest;
import com.redhat.lightwell.model.dto.TransactionResponse;
import com.redhat.lightwell.service.AccountService;
import com.redhat.lightwell.service.TransactionService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    private final AccountService accountService;
    private final TransactionService transactionService;

    public AccountController(AccountService accountService, TransactionService transactionService) {
        this.accountService = accountService;
        this.transactionService = transactionService;
    }

    @GetMapping("/{id}")
    public AccountResponse getById(@PathVariable Long id) {
        return accountService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AccountResponse create(@Valid @RequestBody CreateAccountRequest request) {
        return accountService.create(request);
    }

    @GetMapping("/{id}/balance")
    public BalanceResponse getBalance(@PathVariable Long id) {
        return accountService.getBalance(id);
    }

    @GetMapping("/{id}/transactions")
    public List<TransactionResponse> getTransactions(@PathVariable Long id) {
        return transactionService.getTransactionHistory(id);
    }

    @PostMapping("/{id}/deposit")
    public TransactionResponse deposit(@PathVariable Long id,
                                       @Valid @RequestBody TransactionRequest request) {
        return transactionService.deposit(id, request);
    }

    @PostMapping("/{id}/withdraw")
    public TransactionResponse withdraw(@PathVariable Long id,
                                        @Valid @RequestBody TransactionRequest request) {
        return transactionService.withdraw(id, request);
    }
}
