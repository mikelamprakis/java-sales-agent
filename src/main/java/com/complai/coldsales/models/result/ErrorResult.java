package com.complai.coldsales.models.result;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

/**
 * Error result for pipeline failures.
 * Provides type-safe error handling.
 */
@Getter
public class ErrorResult implements PipelineResult {
    private final String message;
    private final Throwable cause;
    
    public ErrorResult(String message) {
        this(message, null);
    }
    
    public ErrorResult(String message, Throwable cause) {
        this.message = message;
        this.cause = cause;
    }
    
    @Override
    public String getStatus() {
        return "error";
    }

    @Override
    public Map<String, Object> toMap() {
        Map<String, Object> result = new HashMap<>();
        result.put("status", "error");
        result.put("message", message);
        if (cause != null) {
            result.put("error_type", cause.getClass().getSimpleName());
        }
        return result;
    }
    
    @Override
    public String toString() {
        return "ErrorResult{" +
                "message='" + message + '\'' +
                ", cause=" + (cause != null ? cause.getClass().getSimpleName() : "null") +
                '}';
    }
}

