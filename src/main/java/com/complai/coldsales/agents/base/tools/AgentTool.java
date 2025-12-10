package com.complai.coldsales.agents.base.tools;

import com.complai.coldsales.agents.base.client.LLMClient;
import com.complai.coldsales.agents.base.core.Agent;
import com.complai.coldsales.utils.Result;
import com.complai.coldsales.utils.ServiceError;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.concurrent.CompletableFuture;

/**
 * Represents an agent that has been converted to a tool for agent-of-agents pattern.
 */
@Data
@AllArgsConstructor
public class AgentTool {
    
    private String name;
    private String description;
    private Agent agent;
    
    /**
     * Execute this tool by running the underlying agent.
     * 
     * @param llmClient The LLMClient instance to use for executing the agent
     * @param prompt The prompt to execute
     * @return A future containing the tool result as a string
     */
    public CompletableFuture<Result<String, ServiceError>> execute(LLMClient llmClient, String prompt) {
        if (prompt == null || prompt.trim().isEmpty()) {
            return CompletableFuture.completedFuture(Result.<String, ServiceError>ok(""));
        }
        if (llmClient == null) {
            throw new IllegalArgumentException("LLMClient cannot be null");
        }
        return llmClient.run(agent, prompt).thenApply(result -> {
            System.out.println(agent+ "--->"+ prompt);
            if (result == null || result.getFinalOutput() == null) {
                return Result.<String, ServiceError>ok("");
            }

            Object output = result.getFinalOutput();
            System.out.println(agent+ " agent response---->"+ result);
            return Result.<String, ServiceError>ok(output.toString());
        }).exceptionally(throwable -> {
            System.err.println("⚠️  Tool execution error: " + throwable.getMessage());
            return Result.<String, ServiceError>err(new ServiceError(
                    agent.getName(),
                    "execute_tool_" + name,
                    "Tool execution error: " + throwable.getMessage(),
                    throwable
            ));
        });
    }
}

