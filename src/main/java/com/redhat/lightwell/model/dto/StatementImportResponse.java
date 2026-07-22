package com.redhat.lightwell.model.dto;

import java.time.LocalDateTime;

public class StatementImportResponse {

    private final Long accountId;
    private final int transactionsImported;
    private final String status;
    private final LocalDateTime importedAt;

    public StatementImportResponse(Long accountId, int transactionsImported, String status,
                                    LocalDateTime importedAt) {
        this.accountId = accountId;
        this.transactionsImported = transactionsImported;
        this.status = status;
        this.importedAt = importedAt;
    }

    public Long getAccountId() {
        return accountId;
    }

    public int getTransactionsImported() {
        return transactionsImported;
    }

    public String getStatus() {
        return status;
    }

    public LocalDateTime getImportedAt() {
        return importedAt;
    }
}
