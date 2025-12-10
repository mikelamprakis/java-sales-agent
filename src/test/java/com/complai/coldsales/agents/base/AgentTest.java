package com.complai.coldsales.agents.base;

import com.complai.coldsales.models.structured.SalesEmail;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the Agent base class.
 */
class AgentTest {
    
    @Test
    void testAgentCreation() {
        Agent agent = Agent.builder()
                .name("Test Agent")
                .instructions("Test instructions")
                .model("gpt-4o-mini")
                .build();
        
        assertNotNull(agent);
        assertEquals("Test Agent", agent.getName());
        assertEquals("Test instructions", agent.getInstructions());
        assertEquals("gpt-4o-mini", agent.getModel());
    }
    
    @Test
    void testAgentWithStructuredOutput() {
        Agent agent = Agent.builder()
                .name("Structured Agent")
                .instructions("Test instructions")
                .model("gpt-4o-mini")
                .outputType(SalesEmail.class)
                .build();
        
        assertTrue(agent.hasStructuredOutput());
        assertEquals("SalesEmail", agent.getOutputTypeName());
    }
    
    @Test
    void testAgentWithoutStructuredOutput() {
        Agent agent = Agent.builder()
                .name("Simple Agent")
                .instructions("Test instructions")
                .model("gpt-4o-mini")
                .build();
        
        assertFalse(agent.hasStructuredOutput());
        assertEquals("String", agent.getOutputTypeName());
    }
    
    @Test
    void testAgentWithTools() {
        Agent toolAgent = Agent.builder()
                .name("Tool Agent")
                .instructions("Tool instructions")
                .model("gpt-4o-mini")
                .build();
        
        AgentTool tool = new AgentTool("test_tool", "Test tool description", toolAgent);
        
        Agent agent = Agent.builder()
                .name("Agent with Tools")
                .instructions("Test instructions")
                .model("gpt-4o-mini")
                .tools(List.of(tool))
                .build();
        
        assertTrue(agent.hasTools());
        assertEquals(1, agent.getTools().size());
        assertEquals("test_tool", agent.getTools().get(0).getName());
    }
    
    @Test
    void testAgentWithoutTools() {
        Agent agent = Agent.builder()
                .name("Simple Agent")
                .instructions("Test instructions")
                .model("gpt-4o-mini")
                .build();
        
        assertFalse(agent.hasTools());
    }
    
    @Test
    void testAgentAsTool() {
        Agent agent = Agent.builder()
                .name("Convertible Agent")
                .instructions("Test instructions")
                .model("gpt-4o-mini")
                .build();
        
        AgentTool tool = agent.asTool("my_tool", "My tool description");
        
        assertNotNull(tool);
        assertEquals("my_tool", tool.getName());
        assertEquals("My tool description", tool.getDescription());
        assertEquals(agent, tool.getAgent());
    }
    
    @Test
    void testAgentWithGuardrails() {
        GuardrailFunction guardrail = (context, agent, message) -> 
                java.util.concurrent.CompletableFuture.completedFuture(
                    GuardrailResult.pass()
                );
        
        Agent agent = Agent.builder()
                .name("Guarded Agent")
                .instructions("Test instructions")
                .model("gpt-4o-mini")
                .inputGuardrails(List.of(guardrail))
                .build();
        
        assertNotNull(agent.getInputGuardrails());
        assertEquals(1, agent.getInputGuardrails().size());
    }
    
    @Test
    void testAgentDefaultLists() {
        Agent agent = new Agent();
        
        assertNotNull(agent.getInputGuardrails());
        assertNotNull(agent.getTools());
        assertTrue(agent.getInputGuardrails().isEmpty());
        assertTrue(agent.getTools().isEmpty());
    }
}

