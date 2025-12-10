package com.complai.coldsales.models.pipeline.research;

import java.util.List;

/**
 * Represents the complete result of the research pipeline.
 * Contains research data, tool usage info, and summary.
 */
public class ResearchRunResult {
    private final Object finalOutput;
    private final int toolCallsMade;
    private final List<String> toolNames;
    private final String summary;
    
    public ResearchRunResult(Object finalOutput, int toolCallsMade, 
                            List<String> toolNames, String summary) {
        this.finalOutput = finalOutput;
        this.toolCallsMade = toolCallsMade;
        this.toolNames = toolNames;
        this.summary = summary;
    }
    
    public Object getFinalOutput() {
        return finalOutput;
    }
    
    public int getToolCallsMade() {
        return toolCallsMade;
    }
    
    public List<String> getToolNames() {
        return toolNames;
    }
    
    public String getSummary() {
        return summary;
    }
    
    @Override
    public String toString() {
        return "ResearchRunResult{" +
                "finalOutput=" + (finalOutput != null ? finalOutput.getClass().getSimpleName() : "null") +
                ", toolCallsMade=" + toolCallsMade +
                ", toolNames=" + toolNames +
                ", summary='" + (summary != null ? summary.substring(0, Math.min(100, summary.length())) + "..." : "null") + '\'' +
                '}';
    }
}

