package com.complai.coldsales.pipelines;

import com.complai.coldsales.TestUtils;
import com.complai.coldsales.agents.base.core.Agent;
import com.complai.coldsales.agents.base.client.LLMClient;
import com.complai.coldsales.agents.email.StructuredEmailAnalyzerAgent;
import com.complai.coldsales.agents.email.StructuredSubjectWriterAgent;
import com.complai.coldsales.agents.sales.*;
import com.complai.coldsales.agents.email.HTMLConverterAgent;
import com.complai.coldsales.config.Settings;
import com.complai.coldsales.models.pipeline.email.EmailPipelineResult;
import com.complai.coldsales.services.EmailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for EmailPipeline.
 */
class EmailPipelineTest {
    
    private LLMClient llmClient;
    private Settings settings;
    private Agent professionalAgent;
    private Agent engagingAgent;
    private Agent busyAgent;
    private Agent emailAnalyzer;
    private Agent subjectWriter;
    private Agent htmlConverter;
    private EmailService emailService;
    
    @BeforeEach
    void setUp() {
        settings = TestUtils.createTestSettings();
        llmClient = LLMClient.fromSettings(settings);
        emailService = new EmailService(settings);
        
        // Initialize agents (only pass model, not entire Settings)
        professionalAgent = new StructuredProfessionalSalesAgent(settings.getModel(), java.util.List.of()).getAgent();
        engagingAgent = new StructuredEngagingSalesAgent(settings.getModel(), java.util.List.of()).getAgent();
        busyAgent = new StructuredBusySalesAgent(settings.getModel(), java.util.List.of()).getAgent();
        emailAnalyzer = new StructuredEmailAnalyzerAgent(settings.getModel(), java.util.List.of()).getAgent();
        subjectWriter = new StructuredSubjectWriterAgent(settings.getModel(), java.util.List.of()).getAgent();
        htmlConverter = new HTMLConverterAgent(settings.getModel()).getAgent();
    }
    
    @Test
    void testEmailPipelineCreation() {
        EmailPipeline pipeline = new EmailPipeline(
                llmClient, professionalAgent, engagingAgent, busyAgent, emailAnalyzer,
                subjectWriter, htmlConverter, emailService
        );
        
        assertNotNull(pipeline);
    }
    
    @Test
    @org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable(named = "OPENAI_API_KEY", matches = ".+")
    void testEmailPipelineRun() throws ExecutionException, InterruptedException {
        EmailPipeline pipeline = new EmailPipeline(
                llmClient, professionalAgent, engagingAgent, busyAgent, emailAnalyzer,
                subjectWriter, htmlConverter, emailService
        );
        
        String prompt = TestUtils.createTestPrompt();
        CompletableFuture<EmailPipelineResult> future = pipeline.run(prompt);
        EmailPipelineResult result = future.get();
        
        assertNotNull(result);
        assertNotNull(result.getSelectedEmail());
        assertNotNull(result.getAnalysis());
        assertNotNull(result.getSubjectOptions());
    }
}

