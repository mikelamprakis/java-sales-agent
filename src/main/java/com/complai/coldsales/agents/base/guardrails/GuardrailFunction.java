package com.complai.coldsales.agents.base.guardrails;

import com.complai.coldsales.agents.base.core.Agent;
import java.util.concurrent.CompletableFuture;

/**
 * Functional interface for guardrail validation.
 */
@FunctionalInterface
public interface GuardrailFunction {
    
    /**
     * Execute the guardrail check.
     *
     * @param context The execution context
     * @param agent The agent being validated
     * @param message The message to validate
     * @return A future containing the guardrail result
     */
    CompletableFuture<GuardrailResult> execute(Object context, Agent agent, String message);
}

