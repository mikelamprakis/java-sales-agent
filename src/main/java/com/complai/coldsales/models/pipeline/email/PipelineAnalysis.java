package com.complai.coldsales.models.pipeline.email;

/**
 * Represents the analysis results of an email from the pipeline.
 * Part of the email pipeline results.
 * This wraps the main EmailAnalysis model for pipeline-specific use.
 */
public class PipelineAnalysis {
    private final int effectivenessScore;
    private final String personalizationLevel;
    private final String strengths;
    private final String improvementSuggestions;
    
    public PipelineAnalysis(int effectivenessScore, String personalizationLevel, 
                           String strengths, String improvementSuggestions) {
        this.effectivenessScore = effectivenessScore;
        this.personalizationLevel = personalizationLevel;
        this.strengths = strengths;
        this.improvementSuggestions = improvementSuggestions;
    }
    
    public int getEffectivenessScore() {
        return effectivenessScore;
    }
    
    public String getPersonalizationLevel() {
        return personalizationLevel;
    }
    
    public String getStrengths() {
        return strengths;
    }
    
    public String getImprovementSuggestions() {
        return improvementSuggestions;
    }
    
    @Override
    public String toString() {
        return "PipelineAnalysis{" +
                "effectivenessScore=" + effectivenessScore +
                ", personalizationLevel='" + personalizationLevel + '\'' +
                ", strengths='" + strengths + '\'' +
                ", improvementSuggestions='" + improvementSuggestions + '\'' +
                '}';
    }
}

