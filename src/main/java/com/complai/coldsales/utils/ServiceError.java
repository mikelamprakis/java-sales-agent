package com.complai.coldsales.utils;

import java.time.Instant;

/**
 * Typed error representation for service operations.
 * Replaces magic strings and System.err.println with structured error data.
 */
public record ServiceError(
        String service,
        String operation,
        String message,
        Throwable cause,
        Instant timestamp
) {
    public ServiceError(String service, String operation, String message, Throwable cause) {
        this(service, operation, message, cause, Instant.now());
    }
    
    public ServiceError(String service, String operation, String message) {
        this(service, operation, message, null, Instant.now());
    }
    
    /**
     * Format error for logging or user display.
     */
    public String format() {
        StringBuilder sb = new StringBuilder();
        sb.append("⚠️  [").append(service).append("] ").append(operation);
        if (message != null && !message.isBlank()) {
            sb.append(": ").append(message);
        }
        if (cause != null) {
            sb.append(" (cause: ").append(cause.getClass().getSimpleName()).append(")");
        }
        return sb.toString();
    }
    
    /**
     * Get a user-friendly message for display.
     */
    public String getUserMessage() {
        if (message != null && !message.isBlank()) {
            return message;
        }
        return String.format("%s operation failed in %s", operation, service);
    }
}

