package com.complai.coldsales.services;

/**
 * Marker interface for deterministic, non-LLM services/tools (SMTP, scraping, etc).
 */
public interface ServiceTool {

    /**
     * @return human-readable identifier for observability/logging.
     */
    String getServiceName();
}

