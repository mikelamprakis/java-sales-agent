package com.complai.coldsales.models.pipeline.research;

import java.util.List;

/**
 * Internal snapshot of runner state during research pipeline execution.
 * Used internally in the research pipeline.
 */
public class RunnerSnapshot {
    private final Object finalOutput;
    private final int toolCallsMade;
    private final List<String> toolNames;
    
    public RunnerSnapshot(Object finalOutput, int toolCallsMade, List<String> toolNames) {
        this.finalOutput = finalOutput;
        this.toolCallsMade = toolCallsMade;
        this.toolNames = toolNames;
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
    
    @Override
    public String toString() {
        return "RunnerSnapshot{" +
                "finalOutput=" + (finalOutput != null ? finalOutput.getClass().getSimpleName() : "null") +
                ", toolCallsMade=" + toolCallsMade +
                ", toolNames=" + toolNames +
                '}';
    }
}

