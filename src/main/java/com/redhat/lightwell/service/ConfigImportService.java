package com.redhat.lightwell.service;

import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.yaml.snakeyaml.Yaml;

@Service
public class ConfigImportService {

    private final AuditService auditService;
    private final Map<String, Object> currentConfig = new LinkedHashMap<>();

    public ConfigImportService(AuditService auditService) {
        this.auditService = auditService;
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> importConfig(String yamlContent) {
        Yaml yaml = new Yaml();
        Object loaded = yaml.load(yamlContent);

        if (loaded instanceof Map) {
            currentConfig.putAll((Map<String, Object>) loaded);
        }

        auditService.log("CONFIG_IMPORTED", "System", 0L,
                "Imported YAML configuration with " + currentConfig.size() + " keys");

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("status", "SUCCESS");
        response.put("keysImported", currentConfig.size());
        response.put("config", currentConfig);
        return response;
    }

    public Map<String, Object> getCurrentConfig() {
        return new LinkedHashMap<>(currentConfig);
    }
}
