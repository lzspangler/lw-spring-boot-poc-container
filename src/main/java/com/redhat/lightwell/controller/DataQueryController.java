package com.redhat.lightwell.controller;

import java.util.Map;
import javax.validation.Valid;
import com.redhat.lightwell.model.dto.DataQueryRequest;
import com.redhat.lightwell.service.DataQueryService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/query")
public class DataQueryController {

    private final DataQueryService dataQueryService;

    public DataQueryController(DataQueryService dataQueryService) {
        this.dataQueryService = dataQueryService;
    }

    @PostMapping
    public Map<String, Object> query(@Valid @RequestBody DataQueryRequest request) {
        return dataQueryService.query(request.getData(), request.getExpression());
    }
}
