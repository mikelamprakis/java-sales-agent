package com.complai.coldsales.models.pipeline.hybrid;

import java.util.List;

/**
 * Represents the research phase of the hybrid workflow.
 * Contains information about tool usage during research.
 */
public class ResearchPhase {
    private final int toolsUsed;
    private final List<String> toolNames;
    private final String summary;
    
    public ResearchPhase(int toolsUsed, List<String> toolNames, String summary) {
        this.toolsUsed = toolsUsed;
        this.toolNames = toolNames;
        this.summary = summary;
    }
    
    public int getToolsUsed() {
        return toolsUsed;
    }
    
    public List<String> getToolNames() {
        return toolNames;
    }
    
    public String getSummary() {
        return summary;
    }
    
    @Override
    public String toString() {
        return "ResearchPhase{" +
                "toolsUsed=" + toolsUsed +
                ", toolNames=" + toolNames +
                ", summary='" + (summary != null ? summary.substring(0, Math.min(100, summary.length())) + "..." : "null") + '\'' +
                '}';
    }
}

