package com.redhat.lightwell.service;

import java.io.StringReader;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import com.redhat.lightwell.exception.ResourceNotFoundException;
import com.redhat.lightwell.model.Account;
import com.redhat.lightwell.model.Transaction;
import com.redhat.lightwell.model.TransactionType;
import com.redhat.lightwell.model.dto.StatementImportResponse;
import com.redhat.lightwell.repository.AccountRepository;
import com.redhat.lightwell.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StatementImportService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final AuditService auditService;

    public StatementImportService(AccountRepository accountRepository,
                                   TransactionRepository transactionRepository,
                                   AuditService auditService) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.auditService = auditService;
    }

    @Transactional
    public StatementImportResponse importStatement(Long accountId, String xmlContent) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account", accountId));

        List<Transaction> imported = parseAndCreateTransactions(account, xmlContent);

        auditService.log("STATEMENT_IMPORTED", "Account", accountId,
                "Imported " + imported.size() + " transactions from XML statement"
                        + " for account " + account.getAccountNumber());

        return new StatementImportResponse(accountId, imported.size(), "SUCCESS",
                LocalDateTime.now());
    }

    private List<Transaction> parseAndCreateTransactions(Account account, String xmlContent) {
        List<Transaction> transactions = new ArrayList<>();
        XMLInputFactory factory = XMLInputFactory.newInstance();

        try {
            XMLStreamReader reader = factory.createXMLStreamReader(new StringReader(xmlContent));

            String currentElement = null;
            String type = null;
            String amount = null;
            String description = null;

            while (reader.hasNext()) {
                int event = reader.next();

                if (event == XMLStreamConstants.START_ELEMENT) {
                    currentElement = reader.getLocalName();
                } else if (event == XMLStreamConstants.CHARACTERS && currentElement != null) {
                    String text = reader.getText().trim();
                    if (!text.isEmpty()) {
                        switch (currentElement) {
                            case "type":
                                type = text;
                                break;
                            case "amount":
                                amount = text;
                                break;
                            case "description":
                                description = text;
                                break;
                        }
                    }
                } else if (event == XMLStreamConstants.END_ELEMENT
                        && "transaction".equals(reader.getLocalName())) {
                    if (type != null && amount != null) {
                        TransactionType txnType = "DEPOSIT".equalsIgnoreCase(type)
                                ? TransactionType.DEPOSIT : TransactionType.WITHDRAWAL;
                        BigDecimal txnAmount = new BigDecimal(amount);

                        BigDecimal newBalance = txnType == TransactionType.DEPOSIT
                                ? account.getBalance().add(txnAmount)
                                : account.getBalance().subtract(txnAmount);
                        account.setBalance(newBalance);

                        Transaction txn = new Transaction(txnType, txnAmount,
                                description != null ? description : "Imported from statement",
                                account, null, newBalance);
                        txn = transactionRepository.save(txn);
                        transactions.add(txn);
                    }
                    type = null;
                    amount = null;
                    description = null;
                    currentElement = null;
                }
            }

            reader.close();
            accountRepository.save(account);

        } catch (XMLStreamException e) {
            throw new RuntimeException("Failed to parse XML statement", e);
        }

        return transactions;
    }
}
