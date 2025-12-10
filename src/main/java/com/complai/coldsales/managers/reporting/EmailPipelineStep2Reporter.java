package com.complai.coldsales.managers.reporting;

import com.complai.coldsales.models.structured.EmailAnalysis;

import java.util.List;

public class EmailPipelineStep2Reporter extends StepReporter<List<EmailAnalysis>> {
    
    // Optional contextual data provided fluently
    private Integer emailCount;
    
    public EmailPipelineStep2Reporter() {}
    
    public EmailPipelineStep2Reporter withEmailCount(int emailCount) {
        this.emailCount = emailCount;
        return this;
    }
    
    @Override
    protected void startLog() {
        System.out.println("⏳ Step 2/5: Analyzing email effectiveness...");
    }
    
    @Override
    protected void completeLogWithDuration(List<EmailAnalysis> result, long durationMs) {
        System.out.println("✅ Step 2/5: Complete - Email analysis finished");
        int analyzed = emailCount != null ? emailCount : (result != null ? result.size() : 0);
        System.out.println("   - analyzed: " + analyzed + " emails");
        System.out.println("   - duration: " + durationMs + " ms");
        System.out.println();
    }
}

