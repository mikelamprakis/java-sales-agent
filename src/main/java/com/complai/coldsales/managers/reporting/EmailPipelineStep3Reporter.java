package com.complai.coldsales.managers.reporting;

import com.complai.coldsales.models.structured.EmailAnalysis;
import com.complai.coldsales.models.structured.SalesEmail;

import java.util.Map;

public class EmailPipelineStep3Reporter extends StepReporter<Map.Entry<SalesEmail, EmailAnalysis>> {
    
    public EmailPipelineStep3Reporter() {}
    
    @Override
    protected void startLog() {
        System.out.println("⏳ Step 3/5: Selecting best email based on analysis...");
    }
    
    @Override
    protected void completeLogWithDuration(Map.Entry<SalesEmail, EmailAnalysis> result, long durationMs) {
        int score = result.getValue().getEffectivenessScore();
        System.out.println("✅ Step 3/5: Complete - Best email selected (score: " + score + "/10)");
        System.out.println("   - duration: " + durationMs + " ms");
        System.out.println();
    }
    
    // Removed manager dependency
}

