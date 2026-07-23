package com.redhat.lightwell.service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import com.redhat.lightwell.exception.ResourceNotFoundException;
import com.redhat.lightwell.model.Account;
import com.redhat.lightwell.model.AccountType;
import com.redhat.lightwell.model.Customer;
import com.redhat.lightwell.model.Document;
import com.redhat.lightwell.model.dto.DocumentResponse;
import com.redhat.lightwell.repository.AccountRepository;
import com.redhat.lightwell.repository.DocumentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DocumentServiceTest {

    @Mock
    private DocumentRepository documentRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private AuditService auditService;

    @InjectMocks
    private DocumentService documentService;

    private Customer customer;
    private Account account;

    @BeforeEach
    void setUp() {
        customer = new Customer("John", "Doe", "john@example.com", "555-0101");
        account = new Account("ACC-000001", AccountType.CHECKING, new BigDecimal("1000.00"), customer);
    }

    @Test
    void shouldUploadDocumentSuccessfully() throws IOException {
        MultipartFile file = mock(MultipartFile.class);
        when(file.getInputStream()).thenReturn(new ByteArrayInputStream("PDF content".getBytes()));
        when(file.getOriginalFilename()).thenReturn("statement.pdf");
        when(file.getContentType()).thenReturn("application/pdf");
        when(file.getSize()).thenReturn(11L);

        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
        when(documentRepository.save(any(Document.class))).thenAnswer(invocation -> invocation.getArgument(0));

        DocumentResponse response = documentService.upload(1L, file);

        assertThat(response.getFileName()).isEqualTo("statement.pdf");
        assertThat(response.getContentType()).isEqualTo("application/pdf");
        assertThat(response.getFileSize()).isEqualTo(11L);
    }

    @Test
    void shouldThrowNotFoundWhenAccountMissingOnUpload() {
        MultipartFile file = mock(MultipartFile.class);
        when(accountRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> documentService.upload(99L, file))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void shouldThrowRuntimeExceptionWhenFileReadFails() throws IOException {
        MultipartFile file = mock(MultipartFile.class);
        when(file.getInputStream()).thenThrow(new IOException("Disk error"));

        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));

        assertThatThrownBy(() -> documentService.upload(1L, file))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Failed to read uploaded file");
    }

    @Test
    void shouldListDocumentsForAccount() {
        Document doc = new Document("report.pdf", "application/pdf", 100L,
                "data".getBytes(), account);

        when(accountRepository.existsById(1L)).thenReturn(true);
        when(documentRepository.findByAccountId(1L)).thenReturn(List.of(doc));

        List<DocumentResponse> result = documentService.findByAccountId(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getFileName()).isEqualTo("report.pdf");
    }

    @Test
    void shouldThrowNotFoundWhenAccountMissingOnList() {
        when(accountRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> documentService.findByAccountId(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
