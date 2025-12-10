package com.complai.coldsales.models.result;

import java.util.Map;

/**
 * Base interface for all pipeline results.
 * Provides common functionality for email and hybrid workflow results.
 */
public interface PipelineResult {
    
    /**
     * Get the status of the pipeline execution.
     * @return "success" or "error"
     */
    String getStatus();
    
    /**
     * Check if the pipeline execution was successful.
     * @return true if status is "success"
     */
    default boolean isSuccess() {
        return "success".equals(getStatus());
    }
    
    /**
     * Check if the pipeline execution failed.
     * @return true if status is "error"
     */
    default boolean isError() {
        return "error".equals(getStatus());
    }
    
    /**
     * Convert to Map for serialization/API purposes.
     * This should be used for JSON serialization, not for type-safe access.
     * @return Map representation of the result
     */
    Map<String, Object> toMap();
}

