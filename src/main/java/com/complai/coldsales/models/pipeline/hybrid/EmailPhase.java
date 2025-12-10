package com.complai.coldsales.models.pipeline.hybrid;

import java.util.List;

/**
 * Represents the email phase of the hybrid workflow.
 * Contains information about the email generation pipeline execution.
 */
public class EmailPhase {
    private final String pattern;
    private final List<String> steps;
    
    public EmailPhase(String pattern, List<String> steps) {
        this.pattern = pattern;
        this.steps = steps;
    }
    
    public String getPattern() {
        return pattern;
    }
    
    public List<String> getSteps() {
        return steps;
    }
    
    @Override
    public String toString() {
        return "EmailPhase{" +
                "pattern='" + pattern + '\'' +
                ", steps=" + steps +
                '}';
    }
}

