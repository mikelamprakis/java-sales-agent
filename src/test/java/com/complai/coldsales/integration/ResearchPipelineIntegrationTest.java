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
 * Integration tests for the research pipeline workflow.
 * 
 * These tests require OPENAI_API_KEY to be set in the environment.
 */
@EnabledIfEnvironmentVariable(named = "OPENAI_API_KEY", matches = ".+")
class ResearchPipelineIntegrationTest {
    
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
    void testFullResearchPipeline() throws ExecutionException, InterruptedException {
        CompletableFuture<PipelineResult> future = manager.sendPersonalizedColdEmail("Stripe", "CTO");
        PipelineResult result = future.get();
        
        assertNotNull(result);
        
        if (result instanceof HybridResult) {
            HybridResult hybridResult = (HybridResult) result;
            assertTrue(hybridResult.isSuccess());
            assertNotNull(hybridResult.getHybridResult());
            assertNotNull(hybridResult.getResearchPhase());
            assertNotNull(hybridResult.getEmailPhase());
            
            // Verify research phase
            assertTrue(hybridResult.getResearchToolsUsed() >= 0);
            assertNotNull(hybridResult.getResearchToolNames());
            
            // Verify email phase
            assertNotNull(hybridResult.getEmailPhasePattern());
            assertNotNull(hybridResult.getEmailPhaseSteps());
        }
    }
    
    @Test
    void testResearchPipelineWithDifferentCompanies() throws ExecutionException, InterruptedException {
        String[] companies = {"Stripe", "Shopify", "Atlassian"};
        String[] roles = {"CTO", "VP Engineering", "Head of Security"};
        
        for (int i = 0; i < Math.min(companies.length, roles.length); i++) {
            CompletableFuture<PipelineResult> future = 
                    manager.sendPersonalizedColdEmail(companies[i], roles[i]);
            PipelineResult result = future.get();
            
            assertNotNull(result);
            if (result instanceof HybridResult) {
                HybridResult hybridResult = (HybridResult) result;
                assertTrue(hybridResult.isSuccess());
            }
        }
    }
}

