package com.complai.coldsales.integration;

import com.complai.coldsales.TestUtils;
import com.complai.coldsales.agents.base.client.LLMClient;
import com.complai.coldsales.config.Settings;
import com.complai.coldsales.managers.EnhancedSalesManager;
import com.complai.coldsales.models.result.EmailResult;
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
 * Integration tests for the full email pipeline workflow.
 * 
 * These tests require OPENAI_API_KEY to be set in the environment.
 */
@EnabledIfEnvironmentVariable(named = "OPENAI_API_KEY", matches = ".+")
class EmailPipelineIntegrationTest {

    private EnhancedSalesManager manager;
    private ServicesRegistry servicesRegistry;
    
    @BeforeEach
    void setUp() {
        Settings settings = TestUtils.createTestSettings();
        LLMClient llmClient = LLMClient.fromSettings(settings);
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
    void testFullEmailPipeline() throws ExecutionException, InterruptedException {
        String prompt = TestUtils.createTestPrompt();
        
        CompletableFuture<PipelineResult> future = manager.sendStructuredColdEmail(prompt);
        PipelineResult result = future.get();
        
        assertNotNull(result);
        
        if (result instanceof EmailResult) {
            EmailResult emailResult = (EmailResult) result;
            assertTrue(emailResult.isSuccess());
            assertNotNull(emailResult.getEmailPipelineResult());
            assertNotNull(emailResult.getEmailPipelineResult().getSelectedEmail());
            assertNotNull(emailResult.getEmailPipelineResult().getAnalysis());
            assertNotNull(emailResult.getEmailPipelineResult().getSubjectOptions());
            
            // Verify email content
            assertNotNull(emailResult.getEmailPipelineResult().getSelectedEmail().getSubject());
            assertNotNull(emailResult.getEmailPipelineResult().getSelectedEmail().getBody());
        }
    }
    
    @Test
    void testEmailPipelineWithDifferentPrompts() throws ExecutionException, InterruptedException {
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
            }
        }
    }
}

