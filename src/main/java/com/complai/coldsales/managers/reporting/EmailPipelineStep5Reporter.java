package com.complai.coldsales.managers.reporting;

import com.complai.coldsales.services.EmailService;

import java.util.Map;

public class EmailPipelineStep5Reporter extends StepReporter<Map<String, String>>{
    @Override
    protected void startLog() {
        System.out.println("⏳ Step 5/5: Converting to HTML and sending...");
    }

    @Override
    protected void completeLogWithDuration(Map<String, String> result, long durationMs) {
        System.out.println("✅ Step 5/5: Complete - Enhanced email sent!");
        System.out.println("   - duration: " + durationMs + " ms");
        System.out.println();
    }
    
    // Encapsulated supplier
    public java.util.concurrent.CompletableFuture<java.util.Map<String, String>> runWith(
            com.complai.coldsales.agents.base.client.LLMClient llmClient,
            com.complai.coldsales.agents.base.core.Agent htmlConverter,
            EmailService emailService,
            com.complai.coldsales.models.structured.SalesEmail bestEmail,
            com.complai.coldsales.models.structured.EmailSubject subjectData) {
        return runAsync(() -> llmClient.run(htmlConverter, bestEmail.getBody())
                .thenApply(r -> r.getFinalOutput().toString())
                .thenApply(htmlBody -> emailService.sendHtmlEmail(htmlBody, subjectData.getPrimarySubject())));
    }
}
