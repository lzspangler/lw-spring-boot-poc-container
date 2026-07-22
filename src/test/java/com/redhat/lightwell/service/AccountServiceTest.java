package com.redhat.lightwell.service;

import java.math.BigDecimal;
import java.util.Optional;
import com.redhat.lightwell.exception.ResourceNotFoundException;
import com.redhat.lightwell.model.Account;
import com.redhat.lightwell.model.AccountType;
import com.redhat.lightwell.model.Customer;
import com.redhat.lightwell.model.dto.AccountResponse;
import com.redhat.lightwell.model.dto.BalanceResponse;
import com.redhat.lightwell.model.dto.CreateAccountRequest;
import com.redhat.lightwell.repository.AccountRepository;
import com.redhat.lightwell.repository.CustomerRepository;
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
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private AuditService auditService;

    @InjectMocks
    private AccountService accountService;

    @Test
    void shouldCreateAccountSuccessfully() {
        Customer customer = new Customer("John", "Doe", "john@example.com", "555-0101");
        CreateAccountRequest request = new CreateAccountRequest(1L, AccountType.CHECKING);

        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(accountRepository.count()).thenReturn(0L);
        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AccountResponse response = accountService.create(request);

        assertThat(response.getAccountType()).isEqualTo(AccountType.CHECKING);
        assertThat(response.getBalance()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void shouldFindAccountById() {
        Customer customer = new Customer("John", "Doe", "john@example.com", "555-0101");
        Account account = new Account("ACC-000001", AccountType.CHECKING, new BigDecimal("1000.00"), customer);

        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));

        AccountResponse response = accountService.findById(1L);

        assertThat(response.getAccountNumber()).isEqualTo("ACC-000001");
    }

    @Test
    void shouldThrowNotFoundWhenAccountMissing() {
        when(accountRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> accountService.findById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void shouldReturnBalance() {
        Customer customer = new Customer("John", "Doe", "john@example.com", "555-0101");
        Account account = new Account("ACC-000001", AccountType.CHECKING, new BigDecimal("2500.00"), customer);

        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));

        BalanceResponse response = accountService.getBalance(1L);

        assertThat(response.getBalance()).isEqualByComparingTo(new BigDecimal("2500.00"));
        assertThat(response.getAccountNumber()).isEqualTo("ACC-000001");
    }
}
