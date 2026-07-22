package com.redhat.lightwell.controller;

import com.redhat.lightwell.model.dto.StatementImportResponse;
import com.redhat.lightwell.service.StatementImportService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/accounts/{accountId}/import-statement")
public class StatementImportController {

    private final StatementImportService statementImportService;

    public StatementImportController(StatementImportService statementImportService) {
        this.statementImportService = statementImportService;
    }

    @PostMapping(consumes = MediaType.APPLICATION_XML_VALUE)
    public StatementImportResponse importStatement(@PathVariable Long accountId,
                                                    @RequestBody String xmlContent) {
        return statementImportService.importStatement(accountId, xmlContent);
    }
}
