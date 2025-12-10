package com.complai.coldsales.pipelines;

import com.complai.coldsales.TestUtils;
import com.complai.coldsales.agents.base.core.Agent;
import com.complai.coldsales.agents.base.client.LLMClient;
import com.complai.coldsales.agents.research.ProspectResearchAgent;
import com.complai.coldsales.config.Settings;
import com.complai.coldsales.models.pipeline.research.ResearchRunResult;
import com.complai.coldsales.services.ServicesRegistry;
import com.complai.coldsales.services.LinkedInScraperService;
import com.complai.coldsales.services.NewsSearchService;
import com.complai.coldsales.services.WebScraperService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ResearchPipeline.
 */
class ResearchPipelineTest {
    
    private LLMClient llmClient;
    private Settings settings;
    private Agent prospectResearcher;
    private ServicesRegistry servicesRegistry;
    
    @BeforeEach
    void setUp() {
        settings = TestUtils.createTestSettings();
        llmClient = LLMClient.fromSettings(settings);
        servicesRegistry = ServicesRegistry.fromSettings(settings);
        prospectResearcher = new ProspectResearchAgent(
                llmClient,
                settings.getModel(),
                servicesRegistry
        ).getAgent();
    }

    @AfterEach
    void tearDown() {
        if (servicesRegistry != null) {
            servicesRegistry.close();
        }
    }
    
    @Test
    void testResearchPipelineCreation() {
        ResearchPipeline pipeline = new ResearchPipeline(llmClient, prospectResearcher);
        assertNotNull(pipeline);
    }
    
    @Test
    @org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable(named = "OPENAI_API_KEY", matches = ".+")
    void testResearchPipelineRun() throws ExecutionException, InterruptedException {
        ResearchPipeline pipeline = new ResearchPipeline(llmClient, prospectResearcher);
        
        CompletableFuture<ResearchRunResult> future = pipeline.run("Stripe", "CTO");
        ResearchRunResult result = future.get();
        
        assertNotNull(result);
        assertNotNull(result.getSummary());
        assertNotNull(result.getToolCallsMade());
        assertNotNull(result.getToolNames());
    }
    
}

