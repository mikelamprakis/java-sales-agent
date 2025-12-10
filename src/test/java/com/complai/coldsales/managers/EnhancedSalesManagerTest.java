package com.complai.coldsales.managers;

import com.complai.coldsales.TestUtils;
import com.complai.coldsales.agents.base.client.LLMClient;
import com.complai.coldsales.config.Settings;
import com.complai.coldsales.models.result.PipelineResult;
import com.complai.coldsales.services.ServicesRegistry;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for EnhancedSalesManager.
 */
class EnhancedSalesManagerTest {
    
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
    void testEnhancedSalesManagerCreation() {
        assertNotNull(manager);
    }
    
    @Test
    @org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable(named = "OPENAI_API_KEY", matches = ".+")
    void testSendStructuredColdEmail() throws ExecutionException, InterruptedException {
        String prompt = TestUtils.createTestPrompt();
        CompletableFuture<com.complai.coldsales.models.result.PipelineResult> future = 
                manager.sendStructuredColdEmail(prompt);
        PipelineResult result = future.get();
        
        assertNotNull(result);
        assertFalse(result.isSuccess() || result instanceof com.complai.coldsales.models.result.ErrorResult);
    }
    
    @Test
    @org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable(named = "OPENAI_API_KEY", matches = ".+")
    void testSendPersonalizedColdEmail() throws ExecutionException, InterruptedException {
        CompletableFuture<PipelineResult> future = 
                manager.sendPersonalizedColdEmail("Stripe", "CTO");
        PipelineResult result = future.get();
        
        assertNotNull(result);
    }
}

