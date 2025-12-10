package com.complai.coldsales.managers.reporting;

import com.complai.coldsales.agents.base.client.LLMClient;
import com.complai.coldsales.agents.base.result.LLMResult;
import com.complai.coldsales.pipelines.ResearchPipeline;

import java.util.List;

public class ResearchPipelineStep1Reporter extends StepReporter<LLMResult>{
    
    private final String companyName;
    private final String targetRole;
    
    public ResearchPipelineStep1Reporter(String companyName, String targetRole) {
        this.companyName = companyName;
        this.targetRole = targetRole;
    }
    
    @Override
    protected void startLog() {
        System.out.println("📚 PHASE 1: Prospect Research (Agent-of-Agents Pattern)");
        System.out.println("-".repeat(70));
        System.out.println("AI will dynamically decide:");
        System.out.println("  • Which research tools to use");
        System.out.println("  • In what order to use them");
        System.out.println("  • When it has enough information");
        System.out.println("   Target: " + targetRole + " at " + companyName);
        System.out.println();
    }
    
    @Override
    protected void completeLogWithDuration(LLMResult result, long durationMs) {
        System.out.println("✅ Research Phase Complete");
        System.out.println("   Tools Used: " + result.getToolCallsMade());
        System.out.println("   Pattern: Agent-of-Agents (AI decided tool usage)");
        System.out.println("   Duration: " + durationMs + " ms");
        System.out.println();
    }
}
