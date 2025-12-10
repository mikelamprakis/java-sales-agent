package com.complai.coldsales.realapi;

import com.complai.coldsales.TestUtils;
import com.complai.coldsales.agents.base.core.Agent;
import com.complai.coldsales.agents.base.client.LLMClient;
import com.complai.coldsales.agents.email.HTMLConverterAgent;
import com.complai.coldsales.agents.email.StructuredEmailAnalyzerAgent;
import com.complai.coldsales.agents.email.StructuredSubjectWriterAgent;
import com.complai.coldsales.agents.research.ProspectResearchAgent;
import com.complai.coldsales.agents.sales.*;
import com.complai.coldsales.config.Settings;
import com.complai.coldsales.services.ServicesRegistry;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Real API tests for each agent.
 * 
 * These tests make actual calls to OpenAI API and require:
 * - OPENAI_API_KEY to be set in the environment
 * - Valid API key with credits
 * 
 * These tests are marked with @EnabledIfEnvironmentVariable to only run when API key is available.
 */
@EnabledIfEnvironmentVariable(named = "OPENAI_API_KEY", matches = ".+")
class RealApiAgentTests {
    
    private LLMClient llmClient;
    private Settings settings;
    private ServicesRegistry servicesRegistry;
    
    @BeforeEach
    void setUp() {
        settings = TestUtils.createTestSettings();
        llmClient = LLMClient.fromSettings(settings);
        servicesRegistry = ServicesRegistry.fromSettings(settings);
        assertTrue(TestUtils.shouldRunRealApiTests(), "OPENAI_API_KEY must be set for real API tests");
    }

    @AfterEach
    void tearDown() {
        if (servicesRegistry != null) {
            servicesRegistry.close();
        }
    }
    
    @Test
    void testProfessionalSalesAgentRealApi() throws ExecutionException, InterruptedException {
        Agent agent = new StructuredProfessionalSalesAgent(settings.getModel(), java.util.List.of()).getAgent();
        String prompt = TestUtils.createTestPrompt();
        
        CompletableFuture<LLMResult> future = llmClient.run(agent, prompt);
        LLMResult result = future.get();
        
        assertNotNull(result);
        assertNotNull(result.getFinalOutput());
        assertTrue(result.isStructured());
        assertTrue(result.getTokensUsed() > 0);
    }
    
    @Test
    void testEngagingSalesAgentRealApi() throws ExecutionException, InterruptedException {
        Agent agent = new StructuredEngagingSalesAgent(settings.getModel(), java.util.List.of()).getAgent();
        String prompt = TestUtils.createTestPrompt();
        
        CompletableFuture<LLMResult> future = llmClient.run(agent, prompt);
        LLMResult result = future.get();
        
        assertNotNull(result);
        assertNotNull(result.getFinalOutput());
        assertTrue(result.isStructured());
        assertTrue(result.getTokensUsed() > 0);
    }
    
    @Test
    void testBusySalesAgentRealApi() throws ExecutionException, InterruptedException {
        Agent agent = new StructuredBusySalesAgent(settings.getModel(), java.util.List.of()).getAgent();
        String prompt = TestUtils.createTestPrompt();
        
        CompletableFuture<LLMResult> future = llmClient.run(agent, prompt);
        LLMResult result = future.get();
        
        assertNotNull(result);
        assertNotNull(result.getFinalOutput());
        assertTrue(result.isStructured());
        assertTrue(result.getTokensUsed() > 0);
    }
    
    @Test
    void testEmailAnalyzerAgentRealApi() throws ExecutionException, InterruptedException {
        Agent agent = new StructuredEmailAnalyzerAgent(settings.getModel(), java.util.List.of()).getAgent();
        String emailBody = "Hi there,\n\nI wanted to reach out about our SOC2 compliance automation platform. " +
                          "Would you be interested in a 15-minute demo?\n\nBest regards";
        
        CompletableFuture<LLMResult> future = llmClient.run(agent, emailBody);
        LLMResult result = future.get();
        
        assertNotNull(result);
        assertNotNull(result.getFinalOutput());
        assertTrue(result.isStructured());
        assertTrue(result.getTokensUsed() > 0);
    }
    
    @Test
    void testSubjectWriterAgentRealApi() throws ExecutionException, InterruptedException {
        Agent agent = new StructuredSubjectWriterAgent(settings.getModel(), java.util.List.of()).getAgent();
        String emailBody = "Hi there,\n\nI wanted to reach out about our SOC2 compliance automation platform. " +
                          "Would you be interested in a 15-minute demo?\n\nBest regards";
        
        CompletableFuture<LLMResult> future = llmClient.run(agent, emailBody);
        LLMResult result = future.get();
        
        assertNotNull(result);
        assertNotNull(result.getFinalOutput());
        assertTrue(result.isStructured());
        assertTrue(result.getTokensUsed() > 0);
    }
    
    @Test
    void testHTMLConverterAgentRealApi() throws ExecutionException, InterruptedException {
        Agent agent = new HTMLConverterAgent(settings.getModel()).getAgent();
        String emailBody = "Hi there,\n\nI wanted to reach out about our SOC2 compliance automation platform.\n\nBest regards";
        
        CompletableFuture<LLMResult> future = llmClient.run(agent, emailBody);
        LLMResult result = future.get();
        
        assertNotNull(result);
        assertNotNull(result.getFinalOutput());
        assertTrue(result.getTokensUsed() > 0);
    }
    
    @Test
    void testProspectResearchAgentRealApi() throws ExecutionException, InterruptedException {
        Agent agent = new ProspectResearchAgent(
                llmClient,
                settings.getModel(),
                servicesRegistry
        ).getAgent();
        String prompt = "Research Stripe to enable highly personalized cold sales outreach.\n\n" +
                       "Target role: CTO\n" +
                       "Our product: ComplAI - SOC2 compliance automation platform";
        
        CompletableFuture<LLMResult> future = llmClient.run(agent, prompt);
        LLMResult result = future.get();
        
        assertNotNull(result);
        assertNotNull(result.getFinalOutput());
        assertTrue(result.isStructured());
        assertTrue(result.getToolCallsMade() >= 0); // May have tool calls
        assertTrue(result.getTokensUsed() > 0);
    }
}

