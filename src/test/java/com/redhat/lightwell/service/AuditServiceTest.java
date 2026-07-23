package com.redhat.lightwell.service;

import com.redhat.lightwell.model.AuditLog;
import com.redhat.lightwell.repository.AuditLogRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AuditServiceTest {

    @Mock
    private AuditLogRepository auditLogRepository;

    @InjectMocks
    private AuditService auditService;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldLogWithAuthenticatedUser() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("admin", null));

        auditService.log("DEPOSIT", "Account", 1L, "Deposited 500");

        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(captor.capture());

        AuditLog saved = captor.getValue();
        assertThat(saved.getPerformedBy()).isEqualTo("admin");
        assertThat(saved.getAction()).isEqualTo("DEPOSIT");
        assertThat(saved.getEntityType()).isEqualTo("Account");
        assertThat(saved.getEntityId()).isEqualTo(1L);
        assertThat(saved.getDetails()).isEqualTo("Deposited 500");
    }

    @Test
    void shouldLogWithSystemWhenNoAuth() {
        SecurityContextHolder.clearContext();

        auditService.log("STARTUP", "System", 0L, "Application started");

        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(captor.capture());

        assertThat(captor.getValue().getPerformedBy()).isEqualTo("system");
    }

    @Test
    void shouldSaveAuditLogWithCorrectFields() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("user", null));

        auditService.log("TRANSFER", "Account", 42L, "Transfer completed");

        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(captor.capture());

        AuditLog saved = captor.getValue();
        assertThat(saved.getAction()).isEqualTo("TRANSFER");
        assertThat(saved.getEntityType()).isEqualTo("Account");
        assertThat(saved.getEntityId()).isEqualTo(42L);
        assertThat(saved.getDetails()).isEqualTo("Transfer completed");
        assertThat(saved.getPerformedBy()).isEqualTo("user");
    }
}
