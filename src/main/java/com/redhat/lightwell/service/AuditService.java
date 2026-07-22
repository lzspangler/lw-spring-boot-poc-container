package com.redhat.lightwell.service;

import com.redhat.lightwell.model.AuditLog;
import com.redhat.lightwell.repository.AuditLogRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    public AuditService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    public void log(String action, String entityType, Long entityId, String details) {
        String username = resolveUsername();
        AuditLog entry = new AuditLog(action, entityType, entityId, username, details);
        auditLogRepository.save(entry);
    }

    private String resolveUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getName() != null) {
            return auth.getName();
        }
        return "system";
    }
}
