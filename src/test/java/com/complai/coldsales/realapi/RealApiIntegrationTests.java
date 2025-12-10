package com.complai.coldsales.realapi;

import com.complai.coldsales.TestUtils;
import com.complai.coldsales.agents.base.client.LLMClient;
import com.complai.coldsales.config.Settings;
import com.complai.coldsales.managers.EnhancedSalesManager;
import com.complai.coldsales.models.result.EmailResult;
import com.complai.coldsales.models.result.HybridResult;
import com.complai.coldsales.models.result.PipelineResult;
import com.complai.coldsales.services.ServicesRegistry;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Real API integration tests for end-to-end workflows.
 * 
 * These tests make actual calls to OpenAI API and require:
 * - OPENAI_API_KEY to be set in the environment
 * - Valid API key with credits
 * - May take longer to execute due to multiple API calls
 * 
 * These tests verify the complete workflows work with real API calls.
 */
@EnabledIfEnvironmentVariable(named = "OPENAI_API_KEY", matches = ".+")
class RealApiIntegrationTests {
    
    private LLMClient llmClient;
    private Settings settings;
    private EnhancedSalesManager manager;
    private ServicesRegistry servicesRegistry;
    
    @BeforeEach
    void setUp() {
        settings = TestUtils.createTestSettings();
        llmClient = LLMClient.fromSettings(settings);
        assertTrue(TestUtils.shouldRunRealApiTests(), "OPENAI_API_KEY must be set for real API tests");
        servicesRegistry = ServicesRegistry.fromSettings(settings);
        manager = new EnhancedSalesManager(llmClient, settings, servicesRegistry);
    }

    @AfterEach
    void tearDown() {
        if (servicesRegistry != null) {
            servicesRegistry.close();
        }
    }
    
    @Test
    void testFullEmailPipelineRealApi() throws ExecutionException, InterruptedException {
        String prompt = TestUtils.createTestPrompt();
        
        CompletableFuture<PipelineResult> future = manager.sendStructuredColdEmail(prompt);
        PipelineResult result = future.get();
        
        assertNotNull(result);
        assertTrue(result instanceof EmailResult);
        
        EmailResult emailResult = (EmailResult) result;
        assertTrue(emailResult.isSuccess());
        
        // Verify all pipeline stages completed
        assertNotNull(emailResult.getEmailPipelineResult().getSelectedEmail());
        assertNotNull(emailResult.getEmailPipelineResult().getSelectedEmail().getSubject());
        assertNotNull(emailResult.getEmailPipelineResult().getSelectedEmail().getBody());
        assertNotNull(emailResult.getEmailPipelineResult().getAnalysis());
        assertNotNull(emailResult.getEmailPipelineResult().getSubjectOptions());
        
        // Verify email content quality
        String body = emailResult.getEmailPipelineResult().getSelectedEmail().getBody();
        assertTrue(body.length() > 50, "Email body should be substantial");
    }
    
    @Test
    void testFullHybridWorkflowRealApi() throws ExecutionException, InterruptedException {
        CompletableFuture<PipelineResult> future = manager.sendPersonalizedColdEmail("Stripe", "CTO");
        PipelineResult result = future.get();
        
        assertNotNull(result);
        assertTrue(result instanceof HybridResult);
        
        HybridResult hybridResult = (HybridResult) result;
        assertTrue(hybridResult.isSuccess());
        
        // Verify research phase
        assertNotNull(hybridResult.getResearchPhase());
        assertNotNull(hybridResult.getResearchPhase().getSummary());
        assertTrue(hybridResult.getResearchPhase().getSummary().length() > 0);
        
        // Verify email phase
        assertNotNull(hybridResult.getEmailPipelineResult());
        assertNotNull(hybridResult.getEmailPipelineResult().getSelectedEmail());
        assertNotNull(hybridResult.getEmailPipelineResult().getSelectedEmail().getSubject());
        assertNotNull(hybridResult.getEmailPipelineResult().getSelectedEmail().getBody());
        
        // Verify research tools were used
        assertTrue(hybridResult.getResearchToolsUsed() >= 0);
    }
    
    @Test
    void testMultipleEmailGenerationsRealApi() throws ExecutionException, InterruptedException {
        // Test that we can generate multiple emails in sequence
        String[] prompts = {
            "Write a cold sales email for ComplAI targeting a CTO at a fintech company.",
            "Create a personalized email for a SaaS company CEO about SOC2 compliance.",
            "Generate a brief email for a startup founder about compliance automation."
        };
        
        for (String prompt : prompts) {
            CompletableFuture<PipelineResult> future = manager.sendStructuredColdEmail(prompt);
            PipelineResult result = future.get();
            
            assertNotNull(result);
            if (result instanceof EmailResult) {
                EmailResult emailResult = (EmailResult) result;
                assertTrue(emailResult.isSuccess());
                assertNotNull(emailResult.getEmailPipelineResult().getSelectedEmail().getBody());
            }
        }
    }
    
    @Test
    void testResearchWithDifferentCompaniesRealApi() throws ExecutionException, InterruptedException {
        String[] companies = {"Stripe", "Shopify"};
        String[] roles = {"CTO", "VP Engineering"};
        
        for (int i = 0; i < Math.min(companies.length, roles.length); i++) {
            CompletableFuture<PipelineResult> future = 
                    manager.sendPersonalizedColdEmail(companies[i], roles[i]);
            PipelineResult result = future.get();
            
            assertNotNull(result);
            if (result instanceof HybridResult) {
                HybridResult hybridResult = (HybridResult) result;
                assertTrue(hybridResult.isSuccess());
                
                // Verify research was performed
                assertNotNull(hybridResult.getResearchPhase().getSummary());
                
                // Verify email was generated with research context
                String emailBody = hybridResult.getEmailPipelineResult()
                        .getSelectedEmail()
                        .getBody();
                assertNotNull(emailBody);
                assertTrue(emailBody.length() > 50);
            }
        }
    }
}

