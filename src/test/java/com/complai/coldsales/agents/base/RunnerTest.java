package com.complai.coldsales.agents.base;

import com.complai.coldsales.TestUtils;
import com.complai.coldsales.models.structured.SalesEmail;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for LLMClient class.
 * 
 * Note: Some tests require OPENAI_API_KEY to be set for real API calls.
 */
class LLMClientTest {
    
    private LLMClient llmClient;
    
    @BeforeEach
    void setUp() {
        llmClient = LLMClient.builder()
                .apiKey(TestUtils.createTestSettings().getOpenaiApiKey())
                .defaultModel("gpt-4o-mini")
                .build();
    }
    
    @Test
    void testRunWithNullAgent() {
        assertThrows(IllegalArgumentException.class, () -> {
            llmClient.run(null, "test prompt").join();
        });
    }
    
    @Test
    void testRunWithNullPrompt() {
        Agent agent = Agent.builder()
                .name("Test Agent")
                .instructions("Test instructions")
                .model("gpt-4o-mini")
                .build();
        
        assertThrows(IllegalArgumentException.class, () -> {
            llmClient.run(agent, null).join();
        });
    }
    
    @Test
    void testRunWithEmptyPrompt() {
        Agent agent = Agent.builder()
                .name("Test Agent")
                .instructions("Test instructions")
                .model("gpt-4o-mini")
                .build();
        
        assertThrows(IllegalArgumentException.class, () -> {
            llmClient.run(agent, "   ").join();
        });
    }
    
    @Test
    @EnabledIfEnvironmentVariable(named = "OPENAI_API_KEY", matches = ".+")
    void testRunSimpleAgentRealApi() throws ExecutionException, InterruptedException {
        Agent agent = Agent.builder()
                .name("Test Agent")
                .instructions("You are a helpful assistant. Respond with a short greeting.")
                .model("gpt-4o-mini")
                .build();
        
        CompletableFuture<LLMResult> future = llmClient.run(agent, "Say hello");
        LLMResult result = future.get();
        
        assertNotNull(result);
        assertNotNull(result.getFinalOutput());
        assertFalse(result.getFinalOutput().toString().isEmpty());
        assertEquals(0, result.getToolCallsMade());
        assertTrue(result.getTokensUsed() > 0);
    }
    
    @Test
    @EnabledIfEnvironmentVariable(named = "OPENAI_API_KEY", matches = ".+")
    void testRunWithStructuredOutputRealApi() throws ExecutionException, InterruptedException {
        Agent agent = Agent.builder()
                .name("Structured Test Agent")
                .instructions("Generate a sales email")
                .model("gpt-4o-mini")
                .outputType(SalesEmail.class)
                .build();
        
        String prompt = "Write a cold sales email for ComplAI, our SOC2 compliance automation platform. " +
                       "Target: CTO at a SaaS company. Keep it brief.";
        
        CompletableFuture<LLMResult> future = llmClient.run(agent, prompt);
        LLMResult result = future.get();
        
        assertNotNull(result);
        assertNotNull(result.getFinalOutput());
        assertTrue(result.isStructured());
    }
    
    @Test
    void testResultCreation() {
        LLMResult result = new LLMResult("test output");
        
        assertEquals("test output", result.getFinalOutput());
        assertEquals("", result.getModel());
        assertEquals(0, result.getTokensUsed());
        assertFalse(result.isStructured());
        assertEquals(0, result.getToolCallsMade());
        assertTrue(result.getToolsUsed().isEmpty());
    }
    
    @Test
    void testResultBuilder() {
        LLMResult result = LLMResult.builder()
                .finalOutput("test")
                .model("gpt-4o-mini")
                .tokensUsed(100)
                .isStructured(true)
                .toolCallsMade(2)
                .toolsUsed(java.util.List.of("tool1", "tool2"))
                .build();
        
        assertEquals("test", result.getFinalOutput());
        assertEquals("gpt-4o-mini", result.getModel());
        assertEquals(100, result.getTokensUsed());
        assertTrue(result.isStructured());
        assertEquals(2, result.getToolCallsMade());
        assertEquals(2, result.getToolsUsed().size());
    }
}

