package com.redhat.lightwell.service;

import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class ConfigImportServiceTest {

    @Mock
    private AuditService auditService;

    @InjectMocks
    private ConfigImportService configImportService;

    @Test
    void shouldReturnEmptyConfigInitially() {
        Map<String, Object> config = configImportService.getCurrentConfig();

        assertThat(config).isEmpty();
    }

    @Test
    void shouldImportValidYaml() {
        Map<String, Object> result = configImportService.importConfig("database:\n  host: localhost\n");

        assertThat(result.get("status")).isEqualTo("SUCCESS");
        assertThat(result.get("keysImported")).isEqualTo(1);
    }

    @Test
    void shouldMergeMultipleImports() {
        configImportService.importConfig("key1: value1\n");
        configImportService.importConfig("key2: value2\n");

        Map<String, Object> config = configImportService.getCurrentConfig();

        assertThat(config).containsKeys("key1", "key2");
    }

    @Test
    void shouldHandleNonMapYaml() {
        configImportService.importConfig("key1: value1\n");
        configImportService.importConfig("just a scalar string");

        Map<String, Object> config = configImportService.getCurrentConfig();

        assertThat(config).containsKey("key1");
        assertThat(config).hasSize(1);
    }

    @Test
    void shouldReturnDefensiveCopy() {
        configImportService.importConfig("original: data\n");

        Map<String, Object> copy = configImportService.getCurrentConfig();
        copy.put("injected", "value");

        Map<String, Object> actual = configImportService.getCurrentConfig();
        assertThat(actual).doesNotContainKey("injected");
        assertThat(actual).hasSize(1);
    }
}
