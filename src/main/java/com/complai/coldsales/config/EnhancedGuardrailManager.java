package com.complai.coldsales.config;

import com.complai.coldsales.agents.base.core.Agent;
import com.complai.coldsales.agents.base.guardrails.GuardrailFunction;
import com.complai.coldsales.agents.base.guardrails.GuardrailResult;
import com.complai.coldsales.agents.base.client.LLMClient;
import com.complai.coldsales.agents.base.result.LLMResult;
import com.complai.coldsales.models.guardrails.BusinessContextCheck;
import com.complai.coldsales.models.guardrails.ContentSafetyCheck;
import com.complai.coldsales.models.guardrails.PersonalDataCheck;
import com.complai.coldsales.utils.PromptLoader;

import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * Enhanced guardrail manager for sales content validation.
 */
public class EnhancedGuardrailManager {
    
    private final LLMClient llmClient;
    private final String model;
    private Agent contentSafetyAgent;
    private Agent businessContextAgent;
    private Agent personalDataAgent;

    public EnhancedGuardrailManager(LLMClient llmClient, String model) {
        this.llmClient = llmClient;
        this.model = model != null ? model : "gpt-4o-mini";
        initializeGuardrails();
    }

    private void initializeGuardrails() {
        // Content safety guardrail
        contentSafetyAgent = Agent.builder()
                .name("Content Safety Checker")
                .instructions(PromptLoader.loadPrompt("guardrails/content-safety-checker"))
                .outputType(ContentSafetyCheck.class)
                .model(model)
                .build();

        // Business context guardrail
        businessContextAgent = Agent.builder()
                .name("Business Context Checker")
                .instructions(PromptLoader.loadPrompt("guardrails/business-context-checker"))
                .outputType(BusinessContextCheck.class)
                .model(model)
                .build();

        // Personal data guardrail
        personalDataAgent = Agent.builder()
                .name("Personal Data Checker")
                .instructions(PromptLoader.loadPrompt("guardrails/personal-data-checker"))
                .outputType(PersonalDataCheck.class)
                .model(model)
                .build();
    }

    // Get a standard set of guardrails for sales agents.
    public List<GuardrailFunction> getSalesGuardrails() {
        List<GuardrailFunction> guardrails = new ArrayList<>();
        guardrails.add(createContentSafetyGuardrail());
        guardrails.add(createBusinessContextGuardrail());
        guardrails.add(createPersonalDataGuardrail());
        return guardrails;
    }

    // Create a guardrail for content safety validation.
    private GuardrailFunction createContentSafetyGuardrail() {
        return (context, agent, message) -> {
            return llmClient.run(contentSafetyAgent, "Analyze this content for safety: " + message)
                    .thenApply(result -> {
                        Object output = result.getFinalOutput();
                        boolean isUnsafe = checkSafetyTrigger(output);
                        Map<String, Object> info =  Map.of("safety_check", output);
                        return GuardrailResult.builder()
                                .outputInfo(info)
                                .tripwireTriggered(isUnsafe)
                                .build();
                    });
        };
    }

    // Create a guardrail for business context validation.
    private GuardrailFunction createBusinessContextGuardrail() {
        return (context, agent, message) -> {
            return llmClient.run(businessContextAgent, "Analyze this content for business context: " + message)
                    .thenApply(result -> {
                        Object output = result.getFinalOutput();
                        boolean isInappropriate = checkContextTrigger(output);
                        Map<String, Object> info =  Map.of("context_check", output);
                        return GuardrailResult.builder()
                                .outputInfo(info)
                                .tripwireTriggered(isInappropriate)
                                .build();
                    });
        };
    }

    // Create a guardrail for personal data protection.
    private GuardrailFunction createPersonalDataGuardrail() {
        return (context, agent, message) -> {
            return llmClient.run(personalDataAgent, "Analyze this content for personal data: " + message)
                    .thenApply(result -> {
                        Object output = result.getFinalOutput();
                        boolean hasPersonalData = checkDataTrigger(output);
                        Map<String, Object> info = Map.of("data_check", output);
                        return GuardrailResult.builder()
                                .outputInfo(info)
                                .tripwireTriggered(hasPersonalData)
                                .build();
                    });
        };
    }

    // Create a comprehensive guardrail that combines all checks.
    public GuardrailFunction createComprehensiveGuardrail() {
        return (context, agent, message) -> {
            // Run all checks in parallel
            CompletableFuture<LLMResult> safetyFuture = 
                llmClient.run(contentSafetyAgent, "Analyze this content for safety: " + message);
            CompletableFuture<LLMResult> contextFuture = 
                llmClient.run(businessContextAgent, "Analyze this content for business context: " + message);
            CompletableFuture<LLMResult> dataFuture = 
                llmClient.run(personalDataAgent, "Analyze this content for personal data: " + message);

            return CompletableFuture.allOf(safetyFuture, contextFuture, dataFuture)
                    .thenApply(v -> {
                        Object safetyCheck = safetyFuture.join().getFinalOutput();
                        Object contextCheck = contextFuture.join().getFinalOutput();
                        Object dataCheck = dataFuture.join().getFinalOutput();

                        // Check all triggers
                        boolean safetyTriggered = checkSafetyTrigger(safetyCheck);
                        boolean contextTriggered = checkContextTrigger(contextCheck);
                        boolean dataTriggered = checkDataTrigger(dataCheck);

                        boolean isTriggered = safetyTriggered || contextTriggered || dataTriggered;

                        Map<String, Object> info = new HashMap<>();
                        info.put("safety_check", safetyCheck);
                        info.put("context_check", contextCheck);
                        info.put("data_check", dataCheck);

                        return GuardrailResult.builder()
                                .outputInfo(info)
                                .tripwireTriggered(isTriggered)
                                .build();
                    });
        };
    }

    private boolean checkSafetyTrigger(Object output) {
        boolean isUnsafe = false;
        if (output instanceof ContentSafetyCheck) {
            ContentSafetyCheck check = (ContentSafetyCheck) output;
            isUnsafe = check.isContainsSpamIndicators() || check.isInappropriateContent() || (check.getPolicyViolations() != null &&  !check.getPolicyViolations().isEmpty());
        } else {
            String outputStr = output.toString().toLowerCase(); // Fallback: check string for keywords
            isUnsafe = outputStr.contains("urgent") || outputStr.contains("call now") || outputStr.contains("spam") || outputStr.contains("inappropriate") ||outputStr.contains("violation");
        }
        return isUnsafe;
    }

    private boolean checkContextTrigger(Object output) {
        boolean isInappropriate = false;
        if (output instanceof BusinessContextCheck) {
            BusinessContextCheck check = (BusinessContextCheck) output;
            isInappropriate = check.isMentionsCompetitors() || check.isOffBrandMessaging() || (check.getComplianceIssues() != null &&  !check.getComplianceIssues().isEmpty());
        } else {
            // Fallback: check string for competitor names
            String outputStr = output.toString().toLowerCase();
            isInappropriate = outputStr.contains("vanta") || outputStr.contains("drata") || outputStr.contains("competitor") || outputStr.contains("off-brand");
        }
        return isInappropriate;
    }

    private boolean checkDataTrigger(Object output) {
        boolean hasPersonalData = false;
        if (output instanceof PersonalDataCheck) {
            PersonalDataCheck check = (PersonalDataCheck) output;
            hasPersonalData = check.isContainsPersonalNames() ||
                    check.isContainsSensitiveData();
        } else {
            // Fallback: check string for common names
            String outputStr = output.toString().toLowerCase();
            hasPersonalData = outputStr.contains("john") ||
                    outputStr.contains("smith") ||
                    outputStr.contains("mike") ||
                    outputStr.contains("johnson") ||
                    outputStr.contains("personal") ||
                    outputStr.contains("private");
        }
        return hasPersonalData;
    }
}
