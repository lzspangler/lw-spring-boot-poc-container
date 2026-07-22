package com.redhat.lightwell.controller;

import com.redhat.lightwell.model.dto.CreditCheckResponse;
import com.redhat.lightwell.service.CreditCheckService;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/customers/{customerId}/credit-check")
public class CreditCheckController {

    private final CreditCheckService creditCheckService;

    public CreditCheckController(CreditCheckService creditCheckService) {
        this.creditCheckService = creditCheckService;
    }

    @PostMapping
    public CreditCheckResponse performCreditCheck(@PathVariable Long customerId) {
        return creditCheckService.performCreditCheck(customerId);
    }
}
