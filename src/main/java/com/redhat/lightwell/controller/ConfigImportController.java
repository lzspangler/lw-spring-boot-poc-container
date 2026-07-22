package com.redhat.lightwell.controller;

import java.util.Map;
import com.redhat.lightwell.service.ConfigImportService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/config")
public class ConfigImportController {

    private final ConfigImportService configImportService;

    public ConfigImportController(ConfigImportService configImportService) {
        this.configImportService = configImportService;
    }

    @PostMapping(consumes = "text/x-yaml")
    public Map<String, Object> importConfig(@RequestBody String yamlContent) {
        return configImportService.importConfig(yamlContent);
    }

    @GetMapping
    public Map<String, Object> getCurrentConfig() {
        return configImportService.getCurrentConfig();
    }
}
