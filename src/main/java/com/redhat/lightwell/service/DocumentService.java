package com.redhat.lightwell.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.stream.Collectors;
import com.redhat.lightwell.exception.ResourceNotFoundException;
import com.redhat.lightwell.model.Account;
import com.redhat.lightwell.model.Document;
import com.redhat.lightwell.model.dto.DocumentResponse;
import com.redhat.lightwell.repository.AccountRepository;
import com.redhat.lightwell.repository.DocumentRepository;
import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final AccountRepository accountRepository;
    private final AuditService auditService;

    public DocumentService(DocumentRepository documentRepository, AccountRepository accountRepository,
                           AuditService auditService) {
        this.documentRepository = documentRepository;
        this.accountRepository = accountRepository;
        this.auditService = auditService;
    }

    @Transactional
    public DocumentResponse upload(Long accountId, MultipartFile file) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account", accountId));

        byte[] content;
        try (InputStream inputStream = file.getInputStream()) {
            content = IOUtils.toByteArray(inputStream);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read uploaded file", e);
        }

        Document document = new Document(
                file.getOriginalFilename(),
                file.getContentType(),
                file.getSize(),
                content,
                account);
        document = documentRepository.save(document);

        auditService.log("DOCUMENT_UPLOADED", "Document", document.getId(),
                "Uploaded " + file.getOriginalFilename() + " (" + file.getSize()
                        + " bytes) to account " + account.getAccountNumber());

        return toResponse(document);
    }

    public List<DocumentResponse> findByAccountId(Long accountId) {
        if (!accountRepository.existsById(accountId)) {
            throw new ResourceNotFoundException("Account", accountId);
        }
        return documentRepository.findByAccountId(accountId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private DocumentResponse toResponse(Document document) {
        return new DocumentResponse(
                document.getId(),
                document.getFileName(),
                document.getContentType(),
                document.getFileSize(),
                document.getAccount().getId(),
                document.getUploadedAt());
    }
}
