package com.complai.coldsales.agents.base;

import com.complai.coldsales.TestUtils;
import com.complai.coldsales.utils.Result;
import com.complai.coldsales.utils.ServiceError;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for AgentTool.
 */
class AgentToolTest {
    
    private LLMClient llmClient;
    
    @BeforeEach
    void setUp() {
        llmClient = LLMClient.builder()
                .apiKey(TestUtils.createTestSettings().getOpenaiApiKey())
                .defaultModel("gpt-4o-mini")
                .build();
    }
    
    @Test
    void testAgentToolCreation() {
        Agent agent = Agent.builder()
                .name("Test Agent")
                .instructions("Test instructions")
                .model("gpt-4o-mini")
                .build();
        
        AgentTool tool = new AgentTool("test_tool", "Test tool description", agent);
        
        assertNotNull(tool);
        assertEquals("test_tool", tool.getName());
        assertEquals("Test tool description", tool.getDescription());
        assertEquals(agent, tool.getAgent());
    }
    
    @Test
    void testAgentToolExecuteWithNullPrompt() throws ExecutionException, InterruptedException {
        Agent agent = Agent.builder()
                .name("Test Agent")
                .instructions("Test instructions")
                .model("gpt-4o-mini")
                .build();
        
        AgentTool tool = new AgentTool("test_tool", "Test tool description", agent);
        
        CompletableFuture<Result<String, ServiceError>> result = tool.execute(llmClient, null);
        assertTrue(result.get().isOk());
        assertEquals("", result.get().unwrap());
    }
    
    @Test
    void testAgentToolExecuteWithEmptyPrompt() throws ExecutionException, InterruptedException {
        Agent agent = Agent.builder()
                .name("Test Agent")
                .instructions("Test instructions")
                .model("gpt-4o-mini")
                .build();
        
        AgentTool tool = new AgentTool("test_tool", "Test tool description", agent);
        
        CompletableFuture<Result<String, ServiceError>> result = tool.execute(llmClient, "   ");
        assertTrue(result.get().isOk());
        assertEquals("", result.get().unwrap());
    }
}

