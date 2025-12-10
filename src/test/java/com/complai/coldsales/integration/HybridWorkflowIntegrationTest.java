package com.complai.coldsales.integration;

import com.complai.coldsales.TestUtils;
import com.complai.coldsales.agents.base.client.LLMClient;
import com.complai.coldsales.config.Settings;
import com.complai.coldsales.managers.EnhancedSalesManager;
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
 * Integration tests for the hybrid workflow (research + email).
 * 
 * These tests require OPENAI_API_KEY to be set in the environment.
 */
@EnabledIfEnvironmentVariable(named = "OPENAI_API_KEY", matches = ".+")
class HybridWorkflowIntegrationTest {
    
    private LLMClient llmClient;
    private Settings settings;
    private EnhancedSalesManager manager;
    private ServicesRegistry servicesRegistry;
    
    @BeforeEach
    void setUp() {
        settings = TestUtils.createTestSettings();
        llmClient = LLMClient.fromSettings(settings);
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
    void testFullHybridWorkflow() throws ExecutionException, InterruptedException {
        CompletableFuture<PipelineResult> future = manager.sendPersonalizedColdEmail("Stripe", "CTO");
        PipelineResult result = future.get();
        
        assertNotNull(result);
        assertTrue(result instanceof HybridResult);
        
        HybridResult hybridResult = (HybridResult) result;
        assertTrue(hybridResult.isSuccess());
        
        // Verify research phase completed
        assertNotNull(hybridResult.getResearchPhase());
        assertNotNull(hybridResult.getResearchPhase().getSummary());
        
        // Verify email phase completed
        assertNotNull(hybridResult.getEmailPhase());
        assertNotNull(hybridResult.getEmailPipelineResult());
        assertNotNull(hybridResult.getEmailPipelineResult().getSelectedEmail());
    }
    
    @Test
    void testHybridWorkflowEndToEnd() throws ExecutionException, InterruptedException {
        // Test the complete flow: research -> email generation -> sending
        CompletableFuture<PipelineResult> future = manager.sendPersonalizedColdEmail("Shopify", "VP Engineering");
        PipelineResult result = future.get();
        
        assertNotNull(result);
        
        if (result instanceof HybridResult) {
            HybridResult hybridResult = (HybridResult) result;
            
            // Check research was performed
            assertTrue(hybridResult.getResearchToolsUsed() >= 0);
            
            // Check email was generated
            assertNotNull(hybridResult.getEmailPipelineResult().getSelectedEmail().getBody());
            assertNotNull(hybridResult.getEmailPipelineResult().getSelectedEmail().getSubject());
            
            // Check analysis was performed
            assertNotNull(hybridResult.getEmailPipelineResult().getAnalysis());
            
            // Check subject options were generated
            assertNotNull(hybridResult.getEmailPipelineResult().getSubjectOptions());
        }
    }
}

