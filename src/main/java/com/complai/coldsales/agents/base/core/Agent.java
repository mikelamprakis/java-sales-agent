package com.complai.coldsales.agents.base.core;

import com.complai.coldsales.agents.base.guardrails.GuardrailFunction;
import com.complai.coldsales.agents.base.tools.AgentTool;
import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * Base agent class for OpenAI-based agents.
 */
@Data
@Builder
public class Agent {
    
    private String name;
    private String instructions;
    private String model;
    private Class<?> outputType;
    private List<GuardrailFunction> inputGuardrails;
    private List<AgentTool> tools;  // For agent-of-agents pattern

    public Agent() {
        this.inputGuardrails = new ArrayList<>();
        this.tools = new ArrayList<>();
    }

    public Agent(String name, String instructions, String model, Class<?> outputType,
                 List<GuardrailFunction> inputGuardrails, List<AgentTool> tools) {
        this.name = name;
        this.instructions = instructions;
        this.model = model;
        this.outputType = outputType;
        this.inputGuardrails = inputGuardrails != null ? inputGuardrails : new ArrayList<>();
        this.tools = tools != null ? tools : new ArrayList<>();
    }

    /**
     * Check if this agent has structured output requirements.
     */
    public boolean hasStructuredOutput() {
        return outputType != null;
    }

    /**
     * Get the output type name for logging.
     */
    public String getOutputTypeName() {
        return outputType != null ? outputType.getSimpleName() : "String";
    }
    
    /**
     * Check if this agent has tools (agent-of-agents pattern).
     */
    public boolean hasTools() {
        return tools != null && !tools.isEmpty();
    }
    
    /**
     * Convert this agent into a tool that other agents can use.
     * This enables the agent-of-agents pattern.
     */
    public AgentTool asTool(String toolName, String toolDescription) {
        return new AgentTool(toolName, toolDescription, this);
    }
}
