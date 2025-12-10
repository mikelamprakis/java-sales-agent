package com.complai.coldsales.agents.base.tools;

import com.complai.coldsales.agents.base.client.LLMClient;
import com.complai.coldsales.agents.base.core.Agent;
import com.complai.coldsales.services.ServiceTool;
import com.complai.coldsales.utils.Result;
import com.complai.coldsales.utils.ServiceError;

import java.util.concurrent.CompletableFuture;

/**
 * Helper base class for tools that need to invoke deterministic services
 * before delegating to an underlying LLM-backed Agent.
 */
public abstract class ServiceBackedAgentTool extends AgentTool {

    private final ServiceTool service;

    protected ServiceBackedAgentTool(
            String name,
            String description,
            Agent agent,
            ServiceTool service
    ) {
        super(name, description, agent);
        this.service = service;
    }

    protected ServiceTool getService() {
        return service;
    }

    /**
     * Fetch relevant deterministic context (web scraping, news, etc.)
     * before handing control to the LLM agent.
     */
    protected abstract CompletableFuture<String> buildAugmentedPrompt(String originalPrompt);

    @Override
    public CompletableFuture<Result<String, ServiceError>> execute(LLMClient llmClient, String prompt) {
        if (prompt == null || prompt.trim().isEmpty()) {
            return CompletableFuture.completedFuture(Result.ok(""));
        }
        if (llmClient == null) {
            throw new IllegalArgumentException("LLMClient cannot be null");
        }
        return buildAugmentedPrompt(prompt)
                .thenCompose(augmentedPrompt -> super.execute(llmClient, augmentedPrompt))
                .exceptionally(throwable -> {
                    System.err.printf("⚠️  %s tool error: %s%n", service.getServiceName(), throwable.getMessage());
                    return Result.err(new ServiceError(
                            service.getServiceName(),
                            "service_backed_tool_" + getName(),
                            "Error augmenting prompt with service " + service.getServiceName() + ": " + throwable.getMessage(),
                            throwable
                    ));
                });
    }
}

