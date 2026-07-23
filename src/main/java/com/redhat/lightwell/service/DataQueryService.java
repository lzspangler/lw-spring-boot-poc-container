package com.redhat.lightwell.service;

import java.util.LinkedHashMap;
import java.util.Map;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;
import org.springframework.stereotype.Service;

@Service
public class DataQueryService {

    private final AuditService auditService;

    public DataQueryService(AuditService auditService) {
        this.auditService = auditService;
    }

    public Map<String, Object> query(String jsonData, String expression) {
        JSONParser parser = new JSONParser(JSONParser.MODE_RFC4627);

        try {
            Object parsed = parser.parse(jsonData);

            Object result = navigate(parsed, expression);

            auditService.log("DATA_QUERY", "System", 0L,
                    "Executed query with expression: " + expression);

            Map<String, Object> response = new LinkedHashMap<>();
            response.put("expression", expression);
            response.put("result", result != null ? result.toString() : null);
            response.put("resultType", result != null ? result.getClass().getSimpleName() : "null");
            return response;

        } catch (ParseException e) {
            throw new RuntimeException("Failed to parse JSON data", e);
        }
    }

    private Object navigate(Object root, String expression) {
        if (root == null || expression == null || expression.isEmpty()) {
            return root;
        }

        String path = expression.startsWith("$.") ? expression.substring(2) : expression;
        String[] parts = path.split("\\.");

        Object current = root;
        for (String part : parts) {
            if (current instanceof JSONObject) {
                current = ((JSONObject) current).get(part);
            } else if (current instanceof Map) {
                current = ((Map<?, ?>) current).get(part);
            } else {
                return null;
            }
        }
        return current;
    }
}
