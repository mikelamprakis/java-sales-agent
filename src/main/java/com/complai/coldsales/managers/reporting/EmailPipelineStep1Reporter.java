package com.complai.coldsales.managers.reporting;

import com.complai.coldsales.models.structured.SalesEmail;

import java.util.List;

public class EmailPipelineStep1Reporter extends StepReporter<List<SalesEmail>>{
    @Override
    protected void startLog() {
        System.out.println("⏳ Step 1/5: Generating structured email variations...");
        System.out.println("   - Professional sales agent (with guardrails)");
        System.out.println("   - Engaging sales agent (with guardrails)");
        System.out.println("   - Busy sales agent (with guardrails)");
    }

    @Override
    protected void completeLogWithDuration(List<SalesEmail> structuredEmails, long durationMs) {
        System.out.println("✅ Step 1/5: Complete - Generated " + structuredEmails.size() + " structured emails");
        System.out.println("   - duration: " + durationMs + " ms");
        System.out.println();
    }
}
