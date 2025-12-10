package com.complai.coldsales.models.result;

import com.complai.coldsales.models.pipeline.email.EmailPipelineResult;
import com.complai.coldsales.models.pipeline.hybrid.EmailHybridResult;
import com.complai.coldsales.models.pipeline.hybrid.EmailPhase;
import com.complai.coldsales.models.pipeline.hybrid.ResearchPhase;
import lombok.Getter;
import lombok.ToString;

import java.util.List;
import java.util.Map;

/**
 * Type-safe result wrapper for hybrid workflow execution.
 * Combines research phase (agent-of-agents) and email phase (manual orchestration).
 * Replaces Map<String, Object> with proper type-safe access.
 */
@Getter
@ToString
public class HybridResult implements PipelineResult {
    private final EmailHybridResult hybridResult;
    
    public HybridResult(EmailHybridResult hybridResult) {
        this.hybridResult = hybridResult;
    }
    
    @Override
    public String getStatus() {
        return hybridResult.getEmailResult().getStatus();
    }

    public EmailPipelineResult getEmailPipelineResult() {
        return hybridResult.getEmailResult();
    }
    
    public ResearchPhase getResearchPhase() {
        return hybridResult.getResearchPhase();
    }
    
    public EmailPhase getEmailPhase() {
        return hybridResult.getEmailPhase();
    }
    
    // Convenience methods for research phase
    public int getResearchToolsUsed() {
        return getResearchPhase().getToolsUsed();
    }
    
    public List<String> getResearchToolNames() {
        return getResearchPhase().getToolNames();
    }
    
    public String getResearchSummary() {
        return getResearchPhase().getSummary();
    }
    
    // Convenience methods for email phase
    public String getEmailPhasePattern() {
        return getEmailPhase().getPattern();
    }
    
    public List<String> getEmailPhaseSteps() {
        return getEmailPhase().getSteps();
    }
    
    @Override
    public Map<String, Object> toMap() {
        return hybridResult.toMap();
    }

}

