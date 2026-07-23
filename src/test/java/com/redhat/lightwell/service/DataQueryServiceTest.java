package com.redhat.lightwell.service;

import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class DataQueryServiceTest {

    @Mock
    private AuditService auditService;

    @InjectMocks
    private DataQueryService dataQueryService;

    @Test
    void shouldQuerySimpleField() {
        Map<String, Object> result = dataQueryService.query("{\"name\":\"John\"}", "$.name");

        assertThat(result.get("expression")).isEqualTo("$.name");
        assertThat(result.get("result")).isEqualTo("John");
        assertThat(result.get("resultType")).isEqualTo("String");
    }

    @Test
    void shouldQueryNestedField() {
        Map<String, Object> result = dataQueryService.query(
                "{\"user\":{\"profile\":{\"city\":\"Austin\"}}}", "$.user.profile.city");

        assertThat(result.get("result")).isEqualTo("Austin");
    }

    @Test
    void shouldReturnNullForMissingPath() {
        Map<String, Object> result = dataQueryService.query("{\"name\":\"John\"}", "$.nonexistent");

        assertThat(result.get("result")).isNull();
        assertThat(result.get("resultType")).isEqualTo("null");
    }

    @Test
    void shouldThrowForInvalidJson() {
        assertThatThrownBy(() -> dataQueryService.query("<<<not json>>>", "$.x"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Failed to parse JSON data");
    }

    @Test
    void shouldHandleNumericResult() {
        Map<String, Object> result = dataQueryService.query("{\"age\":30}", "$.age");

        assertThat(result.get("result")).isNotNull();
        assertThat(result.get("resultType")).isNotEqualTo("null");
    }

    @Test
    void shouldReturnRootForEmptyExpression() {
        Map<String, Object> result = dataQueryService.query("{\"key\":\"val\"}", "");

        assertThat(result.get("result")).isNotNull();
    }

    @Test
    void shouldQueryWithoutDollarPrefix() {
        Map<String, Object> result = dataQueryService.query("{\"name\":\"John\"}", "name");

        assertThat(result.get("result")).isEqualTo("John");
    }

    @Test
    void shouldReturnNullWhenPathHitsNonObject() {
        Map<String, Object> result = dataQueryService.query(
                "{\"name\":\"John\"}", "$.name.subfield");

        assertThat(result.get("result")).isNull();
        assertThat(result.get("resultType")).isEqualTo("null");
    }
}
