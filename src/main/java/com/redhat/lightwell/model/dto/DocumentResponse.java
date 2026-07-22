package com.redhat.lightwell.model.dto;

import java.time.LocalDateTime;

public class DocumentResponse {

    private final Long id;
    private final String fileName;
    private final String contentType;
    private final Long fileSize;
    private final Long accountId;
    private final LocalDateTime uploadedAt;

    public DocumentResponse(Long id, String fileName, String contentType, Long fileSize,
                            Long accountId, LocalDateTime uploadedAt) {
        this.id = id;
        this.fileName = fileName;
        this.contentType = contentType;
        this.fileSize = fileSize;
        this.accountId = accountId;
        this.uploadedAt = uploadedAt;
    }

    public Long getId() {
        return id;
    }

    public String getFileName() {
        return fileName;
    }

    public String getContentType() {
        return contentType;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public Long getAccountId() {
        return accountId;
    }

    public LocalDateTime getUploadedAt() {
        return uploadedAt;
    }
}
