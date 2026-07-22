package com.redhat.lightwell.service;

import java.math.BigDecimal;
import java.util.Optional;
import com.redhat.lightwell.exception.InsufficientFundsException;
import com.redhat.lightwell.model.Account;
import com.redhat.lightwell.model.AccountType;
import com.redhat.lightwell.model.Customer;
import com.redhat.lightwell.model.Transaction;
import com.redhat.lightwell.model.dto.TransactionRequest;
import com.redhat.lightwell.model.dto.TransactionResponse;
import com.redhat.lightwell.model.dto.TransferRequest;
import com.redhat.lightwell.repository.AccountRepository;
import com.redhat.lightwell.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
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
class TransactionServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private AuditService auditService;

    @InjectMocks
    private TransactionService transactionService;

    private Customer customer;
    private Account account;

    @BeforeEach
    void setUp() {
        customer = new Customer("John", "Doe", "john@example.com", "555-0101");
        account = new Account("ACC-000001", AccountType.CHECKING, new BigDecimal("1000.00"), customer);
    }

    @Test
    void shouldDepositSuccessfully() {
        TransactionRequest request = new TransactionRequest(new BigDecimal("500.00"), "Test deposit");

        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
        when(accountRepository.save(any(Account.class))).thenReturn(account);
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TransactionResponse response = transactionService.deposit(1L, request);

        assertThat(response.getAmount()).isEqualByComparingTo(new BigDecimal("500.00"));
        assertThat(response.getBalanceAfter()).isEqualByComparingTo(new BigDecimal("1500.00"));
    }

    @Test
    void shouldWithdrawSuccessfully() {
        TransactionRequest request = new TransactionRequest(new BigDecimal("300.00"), "Test withdrawal");

        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
        when(accountRepository.save(any(Account.class))).thenReturn(account);
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TransactionResponse response = transactionService.withdraw(1L, request);

        assertThat(response.getAmount()).isEqualByComparingTo(new BigDecimal("300.00"));
        assertThat(response.getBalanceAfter()).isEqualByComparingTo(new BigDecimal("700.00"));
    }

    @Test
    void shouldThrowInsufficientFundsOnOverdraw() {
        TransactionRequest request = new TransactionRequest(new BigDecimal("2000.00"), "Too much");

        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));

        assertThatThrownBy(() -> transactionService.withdraw(1L, request))
                .isInstanceOf(InsufficientFundsException.class);
    }

    @Test
    void shouldTransferSuccessfully() {
        Account target = new Account("ACC-000002", AccountType.SAVINGS, new BigDecimal("500.00"), customer);
        TransferRequest request = new TransferRequest(1L, 2L, new BigDecimal("200.00"), "Transfer");

        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
        when(accountRepository.findById(2L)).thenReturn(Optional.of(target));
        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TransactionResponse response = transactionService.transfer(request);

        assertThat(response.getAmount()).isEqualByComparingTo(new BigDecimal("200.00"));
        assertThat(response.getBalanceAfter()).isEqualByComparingTo(new BigDecimal("800.00"));
    }

    @Test
    void shouldRejectTransferToSameAccount() {
        TransferRequest request = new TransferRequest(1L, 1L, new BigDecimal("100.00"), "Self transfer");

        assertThatThrownBy(() -> transactionService.transfer(request))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
