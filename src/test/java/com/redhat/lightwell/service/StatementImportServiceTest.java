package com.redhat.lightwell.service;

import java.math.BigDecimal;
import java.util.Optional;
import com.redhat.lightwell.exception.ResourceNotFoundException;
import com.redhat.lightwell.model.Account;
import com.redhat.lightwell.model.AccountType;
import com.redhat.lightwell.model.Customer;
import com.redhat.lightwell.model.Transaction;
import com.redhat.lightwell.model.dto.StatementImportResponse;
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
class StatementImportServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private AuditService auditService;

    @InjectMocks
    private StatementImportService statementImportService;

    private Customer customer;
    private Account account;

    @BeforeEach
    void setUp() {
        customer = new Customer("John", "Doe", "john@example.com", "555-0101");
        account = new Account("ACC-000001", AccountType.CHECKING, new BigDecimal("1000.00"), customer);
    }

    @Test
    void shouldImportDepositsAndWithdrawals() {
        String xml = "<statement>"
                + "<transaction><type>DEPOSIT</type><amount>500.00</amount>"
                + "<description>Wire in</description></transaction>"
                + "<transaction><type>WITHDRAWAL</type><amount>200.00</amount>"
                + "<description>Bill pay</description></transaction>"
                + "</statement>";

        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> invocation.getArgument(0));

        StatementImportResponse response = statementImportService.importStatement(1L, xml);

        assertThat(response.getTransactionsImported()).isEqualTo(2);
        assertThat(response.getStatus()).isEqualTo("SUCCESS");
        assertThat(account.getBalance()).isEqualByComparingTo(new BigDecimal("1300.00"));
    }

    @Test
    void shouldThrowNotFoundWhenAccountMissing() {
        when(accountRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> statementImportService.importStatement(99L, "<statement/>"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void shouldThrowRuntimeExceptionForMalformedXml() {
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));

        assertThatThrownBy(() -> statementImportService.importStatement(1L, "<<<not xml>>>"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Failed to parse XML statement");
    }

    @Test
    void shouldHandleEmptyStatement() {
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> invocation.getArgument(0));

        StatementImportResponse response = statementImportService.importStatement(1L, "<statement></statement>");

        assertThat(response.getTransactionsImported()).isEqualTo(0);
        assertThat(response.getStatus()).isEqualTo("SUCCESS");
        assertThat(account.getBalance()).isEqualByComparingTo(new BigDecimal("1000.00"));
    }

    @Test
    void shouldSkipTransactionsWithMissingType() {
        String xml = "<statement>"
                + "<transaction><amount>500.00</amount></transaction>"
                + "</statement>";

        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> invocation.getArgument(0));

        StatementImportResponse response = statementImportService.importStatement(1L, xml);

        assertThat(response.getTransactionsImported()).isEqualTo(0);
        assertThat(account.getBalance()).isEqualByComparingTo(new BigDecimal("1000.00"));
    }

    @Test
    void shouldSkipTransactionsWithMissingAmount() {
        String xml = "<statement>"
                + "<transaction><type>DEPOSIT</type><description>No amount</description></transaction>"
                + "</statement>";

        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> invocation.getArgument(0));

        StatementImportResponse response = statementImportService.importStatement(1L, xml);

        assertThat(response.getTransactionsImported()).isEqualTo(0);
    }

    @Test
    void shouldUseDefaultDescriptionWhenMissing() {
        String xml = "<statement>"
                + "<transaction><type>DEPOSIT</type><amount>100.00</amount></transaction>"
                + "</statement>";

        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> {
            Transaction txn = invocation.getArgument(0);
            assertThat(txn.getDescription()).isEqualTo("Imported from statement");
            return txn;
        });

        statementImportService.importStatement(1L, xml);
    }
}
