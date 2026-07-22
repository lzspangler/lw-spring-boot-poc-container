package com.redhat.lightwell.model.dto;

import java.time.LocalDateTime;

public class CreditCheckResponse {

    private final Long customerId;
    private final int score;
    private final String status;
    private final String provider;
    private final LocalDateTime checkedAt;

    public CreditCheckResponse(Long customerId, int score, String status, String provider,
                                LocalDateTime checkedAt) {
        this.customerId = customerId;
        this.score = score;
        this.status = status;
        this.provider = provider;
        this.checkedAt = checkedAt;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public int getScore() {
        return score;
    }

    public String getStatus() {
        return status;
    }

    public String getProvider() {
        return provider;
    }

    public LocalDateTime getCheckedAt() {
        return checkedAt;
    }
}
