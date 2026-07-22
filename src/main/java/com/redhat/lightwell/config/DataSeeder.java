package com.redhat.lightwell.config;

import java.math.BigDecimal;
import java.util.List;
import com.redhat.lightwell.model.Account;
import com.redhat.lightwell.model.AccountType;
import com.redhat.lightwell.model.Customer;
import com.redhat.lightwell.model.Transaction;
import com.redhat.lightwell.model.TransactionType;
import com.redhat.lightwell.repository.AccountRepository;
import com.redhat.lightwell.repository.CustomerRepository;
import com.redhat.lightwell.repository.TransactionRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DataSeeder implements CommandLineRunner {

    private final CustomerRepository customerRepository;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    public DataSeeder(CustomerRepository customerRepository,
                      AccountRepository accountRepository,
                      TransactionRepository transactionRepository) {
        this.customerRepository = customerRepository;
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        Customer john = new Customer("John", "Doe", "john.doe@example.com", "555-0101");
        Customer jane = new Customer("Jane", "Smith", "jane.smith@example.com", "555-0102");
        customerRepository.saveAll(List.of(john, jane));

        Account johnsChecking = new Account("ACC-000001", AccountType.CHECKING,
                new BigDecimal("2500.00"), john);
        Account johnsSavings = new Account("ACC-000002", AccountType.SAVINGS,
                new BigDecimal("10000.00"), john);
        Account janesChecking = new Account("ACC-000003", AccountType.CHECKING,
                new BigDecimal("5000.00"), jane);
        accountRepository.saveAll(List.of(johnsChecking, johnsSavings, janesChecking));

        Transaction t1 = new Transaction(TransactionType.DEPOSIT, new BigDecimal("1000.00"),
                "Initial deposit", johnsChecking, null, new BigDecimal("1000.00"));
        Transaction t2 = new Transaction(TransactionType.DEPOSIT, new BigDecimal("1500.00"),
                "Payroll deposit", johnsChecking, null, new BigDecimal("2500.00"));
        transactionRepository.saveAll(List.of(t1, t2));
    }
}
