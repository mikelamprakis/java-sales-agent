package com.complai.coldsales.models.pipeline.hybrid;

import com.complai.coldsales.models.pipeline.email.EmailPipelineResult;
import com.complai.coldsales.models.result.PipelineResult;

import java.util.List;
import java.util.Map;

/**
 * Represents the complete result of the hybrid workflow.
 * Combines both research phase (agent-of-agents) and email phase (manual orchestration).
 */
public class EmailHybridResult implements PipelineResult {
    private final EmailPipelineResult emailResult;
    private final ResearchPhase researchPhase;
    private final EmailPhase emailPhase;
    
    public EmailHybridResult(EmailPipelineResult emailResult, 
                            ResearchPhase researchPhase, 
                            EmailPhase emailPhase) {
        this.emailResult = emailResult;
        this.researchPhase = researchPhase;
        this.emailPhase = emailPhase;
    }
    
    public EmailPipelineResult getEmailResult() {
        return emailResult;
    }
    
    public ResearchPhase getResearchPhase() {
        return researchPhase;
    }
    
    public EmailPhase getEmailPhase() {
        return emailPhase;
    }
    
    @Override
    public String getStatus() {
        return emailResult.getStatus();
    }
    
    @Override
    public Map<String, Object> toMap() {
        Map<String, Object> base = emailResult.toMap();
        base.put("pattern", "hybrid");
        base.put("research_phase", Map.of(
                "pattern", "agent-of-agents",
                "tools_used", researchPhase.getToolsUsed(),
                "tool_names", researchPhase.getToolNames(),
                "research_summary", researchPhase.getSummary()
        ));
        base.put("email_phase", Map.of(
                "pattern", emailPhase.getPattern(),
                "steps", emailPhase.getSteps()
        ));
        return base;
    }
    
    @Override
    public String toString() {
        return "EmailHybridResult{" +
                "emailResult=" + emailResult +
                ", researchPhase=" + researchPhase +
                ", emailPhase=" + emailPhase +
                '}';
    }
}

