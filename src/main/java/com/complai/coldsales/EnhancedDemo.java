package com.complai.coldsales;

import com.complai.coldsales.agents.base.client.LLMClient;
import com.complai.coldsales.config.Settings;
import com.complai.coldsales.managers.EnhancedSalesManager;
import com.complai.coldsales.models.result.PipelineResult;
import com.complai.coldsales.services.ServicesRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.complai.coldsales.Logs.displayResult;
import static com.complai.coldsales.Logs.startingLogs;

/**
 * Enhanced Sales Agent Demo - Single Entrypoint with Toggle
 * 
 * This demonstrates BOTH agentic patterns:
 * 1. Agent-of-Agents: Prospect research (optional, controlled by USE_RESEARCH flag)
 * 2. Manual Orchestration: Email generation (always runs)
 * 
 * Edit the configuration below and run!
 */
public class EnhancedDemo {
    
    private static final Logger log = LoggerFactory.getLogger(EnhancedDemo.class);

    // Toggle research phase (agent-of-agents pattern)
    static final boolean USE_RESEARCH = true;  // Set too false to skip research
    // If USE_RESEARCH = true, configure target company:
    static final String COMPANY_NAME = "Stripe";
    static final String TARGET_ROLE = "CTO";

    // If USE_RESEARCH = false, provide direct prompt:
    private static final String DIRECT_PROMPT = 
        "Write a cold sales email for ComplAI, our SOC2 compliance automation platform.\n\n" +
        "Target: CTO at a 200-person SaaS company\n" +
        "Pain point: Manual audit preparation taking weeks of engineering time\n" +
        "Value prop: Reduce audit prep from weeks to days with AI automation\n" +
        "Goal: Schedule a 15-minute demo call";

    // ============================================================================

    public static void main(String[] args) {
        startingLogs();
        try {
            // Load settings
            Settings settings = Settings.loadSettings();
            log.info("✅ Configuration loaded");
            log.info("📧 From: {}", settings.getFromEmail());
            log.info("📧 To: {}", settings.getToEmail());
            
            // Create LLMClient instance (reused across all agents for better performance)
            try (LLMClient llmClient = LLMClient.fromSettings(settings);
                ServicesRegistry servicesRegistry = ServicesRegistry.fromSettings(settings)) {
                log.info("✅ LLMClient initialized with cached OpenAI client");

                EnhancedSalesManager manager = new EnhancedSalesManager(llmClient, settings, servicesRegistry);

                PipelineResult result = USE_RESEARCH ?
                        manager.sendPersonalizedColdEmail(COMPANY_NAME, TARGET_ROLE).join() // HYBRID WORKFLOW: Research + Email
                        :
                        manager.sendStructuredColdEmail(DIRECT_PROMPT).join(); // MANUAL ORCHESTRATION ONLY: Direct prompt to email

                displayResult(result);
            }
        } catch (Exception e) {
            handleError(e);
        }
    }

    private static void handleError(Exception e){
        log.error("❌ Fatal error occurred", e);
        System.exit(1);
    }

}
