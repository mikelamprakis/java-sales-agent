package com.complai.coldsales.agents.base.core;

import com.complai.coldsales.agents.base.guardrails.GuardrailFunction;
import com.complai.coldsales.agents.base.tools.AgentTool;
import com.complai.coldsales.agents.base.tools.ServiceBackedAgentTool;
import com.complai.coldsales.utils.PromptLoader;
import lombok.Getter;

import java.util.Collections;
import java.util.List;

/**
 * Base class for AI-backed agents that rely on the LLMClient/LLM stack.
 * Centralizes agent construction so subclasses only need to provide:
 * - Agent name
 * - Model
 * - Prompt ID (for instructions)
 * - Optional: output type, guardrails, tools
 * 
 * This eliminates duplication of Agent.builder() calls across subclasses.
 */
@Getter
public abstract class AIAgentComponent {

    protected final Agent agent;

    /**
     * Constructs an agent using declarative configuration from subclasses.
     * 
     * @param model The LLM model to use (e.g., "gpt-4o-mini")
     */
    protected AIAgentComponent(String model) {
        Agent.AgentBuilder builder = Agent.builder()
                .name(getAgentName())
                .instructions(getInstruction())
                .model(model);

        // Optional configurations
        Class<?> outputType = getOutputType();
        if (outputType != null) {
            builder.outputType(outputType);
        }

        List<GuardrailFunction> guardrails = getGuardrails();
        if (guardrails != null && !guardrails.isEmpty()) {
            builder.inputGuardrails(guardrails);
        }

        List<AgentTool> tools = getTools();
        if (tools != null && !tools.isEmpty()) {
            builder.tools(tools);
        }

        this.agent = builder.build();
    }

    /**
     * Get the agent's display name. Must be implemented by subclasses.
     */
    protected abstract String getAgentName();

    /**
     * Get the prompt ID to load instructions from. Must be implemented by subclasses.
     * The prompt will be loaded via PromptLoader.loadPrompt(promptId).
     */
    protected abstract String getPromptId();

    /**
     * Load instructions from the prompt file using the prompt ID.
     */
    protected String getInstruction() {
        return PromptLoader.loadPrompt(getPromptId());
    }

    /**
     * Optional: Return the output type for structured outputs (e.g., SalesEmail.class).
     * Return null for text outputs.
     */
    protected Class<?> getOutputType() {
        return null;
    }

    /**
     * Optional: Return guardrails for input validation.
     * Return null or empty list for no guardrails.
     */
    protected List<GuardrailFunction> getGuardrails() {
        return Collections.emptyList();
    }

    /**
     * Optional: Return tools for agent-of-agents pattern.
     * Return null or empty list for no tools.
     */
    protected List<AgentTool> getTools() {
        return Collections.emptyList();
    }

    /**
     * Optional: Create a service-backed agent tool.
     * Override in subclasses that wrap services (e.g., CompanyWebsiteAnalyzerAgent).
     */
    public ServiceBackedAgentTool getServiceBackedAgentTool() {
        return null;
    }
}

