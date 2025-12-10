package com.complai.coldsales.managers;

import com.complai.coldsales.agents.base.core.Agent;
import com.complai.coldsales.agents.base.guardrails.GuardrailFunction;
import com.complai.coldsales.agents.base.client.LLMClient;
import com.complai.coldsales.agents.email.HTMLConverterAgent;
import com.complai.coldsales.agents.email.StructuredEmailAnalyzerAgent;
import com.complai.coldsales.agents.email.StructuredSubjectWriterAgent;
import com.complai.coldsales.agents.research.ProspectResearchAgent;
import com.complai.coldsales.agents.sales.*;
import com.complai.coldsales.config.EnhancedGuardrailManager;
import com.complai.coldsales.config.Settings;
import com.complai.coldsales.services.EmailService;
import com.complai.coldsales.services.ServicesRegistry;
import com.complai.coldsales.models.pipeline.email.EmailPipelineResult;
import com.complai.coldsales.models.pipeline.hybrid.EmailHybridResult;
import com.complai.coldsales.models.pipeline.hybrid.EmailPhase;
import com.complai.coldsales.models.pipeline.hybrid.ResearchPhase;
import com.complai.coldsales.models.pipeline.research.ResearchRunResult;
import com.complai.coldsales.models.result.EmailResult;
import com.complai.coldsales.models.result.ErrorResult;
import com.complai.coldsales.models.result.HybridResult;
import com.complai.coldsales.models.result.PipelineResult;
import com.complai.coldsales.pipelines.EmailPipeline;
import com.complai.coldsales.pipelines.ResearchPipeline;
import com.complai.coldsales.services.EmailService;
import java.util.*;
import java.util.concurrent.CompletableFuture;

import static com.complai.coldsales.Logs.*;

/**
 * Enhanced sales manager with structured outputs and guardrails.
 */
public class EnhancedSalesManager implements SalesManager{

    private final LLMClient llmClient;
    private final Settings settings;
    private final EmailService emailService;
    private final EnhancedGuardrailManager guardrailManager;
    
    private final Agent professionalAgent;
    private final Agent engagingAgent;
    private final Agent busyAgent;
    private final Agent emailAnalyzer;
    private final Agent subjectWriter;
    private final Agent htmlConverter;
    private final Agent prospectResearcher;

    public EnhancedSalesManager(LLMClient llmClient, Settings settings) {
        this(llmClient, settings, ServicesRegistry.fromSettings(settings));
    }

    public EnhancedSalesManager(LLMClient llmClient, Settings settings, ServicesRegistry servicesRegistry) {
        System.out.println("🤖 Initializing Enhanced Sales Manager...");
        this.llmClient = llmClient;
        this.settings = settings;
        this.emailService = servicesRegistry.getEmailService();
        String model = settings.getModel();

        // Initialize guardrail manager
        this.guardrailManager = new EnhancedGuardrailManager(llmClient, model);
        List<GuardrailFunction> salesGuardrails = guardrailManager.getSalesGuardrails();
        
        // Initialize structured sales agents with guardrails (only pass model, not entire Settings)
        this.professionalAgent = new StructuredProfessionalSalesAgent(model, salesGuardrails).getAgent();
        this.engagingAgent = new StructuredEngagingSalesAgent(model, salesGuardrails).getAgent();
        this.busyAgent = new StructuredBusySalesAgent(model, salesGuardrails).getAgent();
        
        // Initialize analysis and processing agents (only pass model, not entire Settings)
        this.emailAnalyzer = new StructuredEmailAnalyzerAgent(model, salesGuardrails).getAgent();
        this.subjectWriter = new StructuredSubjectWriterAgent(model, salesGuardrails).getAgent();
        this.htmlConverter = new HTMLConverterAgent(model).getAgent();
        //this.htmlEmailSender = new HTMLEmailSenderAgent(model, emailService).getAgent();

        // Agent-of-agents pattern: Prospect research with dynamic tool selection (only pass model, not entire Settings)
        this.prospectResearcher = new ProspectResearchAgent(llmClient, model, servicesRegistry).getAgent();
        System.out.println("✅ Manager initialized" + "\n");
    }


    @Override
    public CompletableFuture<PipelineResult> sendStructuredColdEmail(String message) {
        System.out.println("🎯 Running MANUAL ORCHESTRATION...\n" + "   Fixed pipeline: generate → analyze → select → send\n");
        EmailPipeline pipeline = new EmailPipeline(llmClient, professionalAgent, engagingAgent, busyAgent, emailAnalyzer, subjectWriter, htmlConverter, emailService);
        return pipeline.run(message)
                .thenApply(result -> (PipelineResult) new EmailResult(result))
                .exceptionally(throwable -> (PipelineResult) handlePipelineError(throwable, "Error in sendStructuredColdEmail"));
    }

    @Override
    public CompletableFuture<PipelineResult> sendPersonalizedColdEmail(String companyName, String targetRole) {
        System.out.println("🎯 Running HYBRID WORKFLOW...\n" + "   Phase 1: Agent-of-Agents (Prospect Research)\n" + "   Phase 2: Manual Orchestration (Email Generation)\n");
        return CompletableFuture.supplyAsync(() -> {
            logStartForHybridFlow(targetRole,companyName);
            try {
                // PATTERN 1: AGENT-OF-AGENTS (Research Phase)
                ResearchRunResult researchResult = new ResearchPipeline(llmClient, prospectResearcher).run(companyName, targetRole).join();
                String researchSummary = researchResult.getSummary();

                // PATTERN 2: MANUAL ORCHESTRATION (Email Phase)
                logPhase2();
                String enhancedPrompt = getEnhancedPrompt(targetRole, companyName, researchSummary);
                EmailPipelineResult emailResult = new EmailPipeline(llmClient, professionalAgent, engagingAgent, busyAgent, emailAnalyzer, subjectWriter, htmlConverter, emailService)
                        .run(enhancedPrompt).join(); // Run the EXISTING manual orchestration pipeline

                EmailHybridResult hybrid = new EmailHybridResult(
                        emailResult,
                        new ResearchPhase(researchResult.getToolCallsMade(), researchResult.getToolNames(), researchSummary),
                        new EmailPhase(
                                "manual-orchestration",
                                Arrays.asList("generate", "analyze", "select", "subject", "html", "send")
                        )
                );
                logEndOfHybridFlow(hybrid);
                return (PipelineResult) new HybridResult(hybrid);
            } catch (Exception e) {
                return (PipelineResult) handlePipelineError(e, "Error in hybrid workflow");
            }
        });
    }

    private String getEnhancedPrompt(String targetRole, String companyName, String researchSummary){
        return "Write a highly personalized cold sales email for ComplAI " +
                "(SOC2 compliance automation platform).\n\n" +
                "Target: " + targetRole + " at " + companyName + "\n\n" +
                "RESEARCH INSIGHTS:\n" + researchSummary + "\n\n" +
                "REQUIREMENTS:\n" +
                "- Use the research insights to make the email feel personal and relevant\n" +
                "- Reference specific pain points or opportunities identified in research\n" +
                "- Mention recent news/events if available\n" +
                "- Keep tone professional but approachable\n" +
                "- Clear call to action for a 15-minute demo";
    }

    // Error handler - Returns type-safe ErrorResult instead of Map
    private ErrorResult handlePipelineError(Throwable e, String prefixMessage) {
        System.err.println("❌ " + prefixMessage +" : " + e.getMessage());
        e.printStackTrace();
        return new ErrorResult(e.getMessage(), e);
    }

}

