package com.redhat.lightwell.model.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

public class DataQueryRequest {

    @NotNull(message = "Data is required")
    private String data;

    @NotBlank(message = "Expression is required")
    private String expression;

    public DataQueryRequest() {
    }

    public DataQueryRequest(String data, String expression) {
        this.data = data;
        this.expression = expression;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getExpression() {
        return expression;
    }

    public void setExpression(String expression) {
        this.expression = expression;
    }
}
