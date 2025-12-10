package com.complai.coldsales.managers;

import com.complai.coldsales.models.result.PipelineResult;

import java.util.concurrent.CompletableFuture;

public interface SalesManager {

    /**
     * Send a cold email using structured outputs and enhanced analysis.
     *
     * @param message The prompt for email generation
     * @return Type-safe EmailResult (not Map!)
     */
    public CompletableFuture<PipelineResult> sendStructuredColdEmail(String message);

    /**
     * HYBRID WORKFLOW: Combines BOTH agentic patterns.
     * <p>
     * PATTERN 1 - Agent-of-Agents (Research Phase):
     * - AI decides which research tools to use
     * - Dynamic, exploratory workflow
     * <p>
     * PATTERN 2 - Manual Orchestration (Email Phase):
     * - Fixed sequence with research data
     * - Predictable, deterministic workflow
     *
     * @param companyName Target company name
     * @param targetRole Target role (e.g., "CTO")
     * @return Type-safe HybridResult (not Map!)
     */
    public CompletableFuture<PipelineResult> sendPersonalizedColdEmail(String companyName, String targetRole);
}
