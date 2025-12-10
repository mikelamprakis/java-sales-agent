package com.complai.coldsales.agents.sales;

import com.complai.coldsales.TestUtils;
import com.complai.coldsales.agents.base.core.Agent;
import com.complai.coldsales.agents.email.StructuredEmailAnalyzerAgent;
import com.complai.coldsales.agents.email.StructuredSubjectWriterAgent;
import com.complai.coldsales.config.Settings;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for all sales agents.
 */
class SalesAgentsTest {
    
    private Settings settings;
    
    @BeforeEach
    void setUp() {
        settings = TestUtils.createTestSettings();
    }
    
    @Test
    void testStructuredProfessionalSalesAgent() {
        StructuredProfessionalSalesAgent agentWrapper = new StructuredProfessionalSalesAgent(settings.getModel(), java.util.List.of());
        Agent agent = agentWrapper.getAgent();
        
        assertNotNull(agent);
        assertEquals("Professional Sales Agent (Structured)", agent.getName());
        assertTrue(agent.hasStructuredOutput());
        assertEquals(settings.getModel(), agent.getModel());
    }
    
    @Test
    void testStructuredEngagingSalesAgent() {
        StructuredEngagingSalesAgent agentWrapper = new StructuredEngagingSalesAgent(settings.getModel(), java.util.List.of());
        Agent agent = agentWrapper.getAgent();
        
        assertNotNull(agent);
        assertEquals("Engaging Sales Agent (Structured)", agent.getName());
        assertTrue(agent.hasStructuredOutput());
        assertEquals(settings.getModel(), agent.getModel());
    }
    
    @Test
    void testStructuredBusySalesAgent() {
        StructuredBusySalesAgent agentWrapper = new StructuredBusySalesAgent(settings.getModel(), java.util.List.of());
        Agent agent = agentWrapper.getAgent();
        
        assertNotNull(agent);
        assertEquals("Busy Sales Agent (Structured)", agent.getName());
        assertTrue(agent.hasStructuredOutput());
        assertEquals(settings.getModel(), agent.getModel());
    }
    
    @Test
    void testStructuredEmailAnalyzerAgent() {
        StructuredEmailAnalyzerAgent agentWrapper = new StructuredEmailAnalyzerAgent(settings.getModel(), java.util.List.of());
        Agent agent = agentWrapper.getAgent();
        
        assertNotNull(agent);
        assertTrue(agent.getName().contains("Email Analyzer"));
        assertTrue(agent.hasStructuredOutput());
        assertEquals(settings.getModel(), agent.getModel());
    }
    
    @Test
    void testStructuredSubjectWriterAgent() {
        StructuredSubjectWriterAgent agentWrapper = new StructuredSubjectWriterAgent(settings.getModel(), java.util.List.of());
        Agent agent = agentWrapper.getAgent();
        
        assertNotNull(agent);
        assertTrue(agent.getName().contains("Subject"));
        assertTrue(agent.hasStructuredOutput());
        assertEquals(settings.getModel(), agent.getModel());
    }
    
    @Test
    void testSalesAgentsWithGuardrails() {
        com.complai.coldsales.agents.base.GuardrailFunction guardrail = (context, agent, message) -> 
            java.util.concurrent.CompletableFuture.completedFuture(
                com.complai.coldsales.agents.base.GuardrailResult.pass()
            );
        
        StructuredProfessionalSalesAgent agentWrapper = new StructuredProfessionalSalesAgent(
                settings.getModel(), java.util.List.of(guardrail));
        Agent agent = agentWrapper.getAgent();
        
        assertNotNull(agent);
        assertNotNull(agent.getInputGuardrails());
        assertEquals(1, agent.getInputGuardrails().size());
    }
}

