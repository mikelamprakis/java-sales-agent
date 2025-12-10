package com.complai.coldsales.models.structured;

import com.complai.coldsales.models.types.EmailTone;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Structured output for email analysis and evaluation.
 * This is what the LLM agent returns when analyzing an email.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailAnalysis {
    
    @JsonProperty("effectiveness_score")
    private int effectivenessScore;
    
    @JsonProperty("tone")
    private String tone;
    
    @JsonProperty("word_count")
    private int wordCount;
    
    @JsonProperty("has_call_to_action")
    private boolean hasCallToAction;
    
    @JsonProperty("personalization_level")
    private String personalizationLevel; // "low", "medium", "high"
    
    @JsonProperty("improvement_suggestions")
    private String improvementSuggestions;
    
    @JsonProperty("strengths")
    private String strengths;
    
    @JsonProperty("weaknesses")
    private String weaknesses;

    public EmailTone getToneEnum() {
        return EmailTone.fromString(tone);
    }

    public static EmailAnalysis fallback(Object output){
        return EmailAnalysis.builder()
                .effectivenessScore(5)
                .tone("professional")
                .wordCount(output.toString().split("\\s+").length)
                .hasCallToAction(true)
                .personalizationLevel("medium")
                .improvementSuggestions("Consider more specific value propositions")
                .strengths("Clear and professional")
                .weaknesses("Could be more personalized")
                .build();
    }
    
    @Override
    public String toString() {
        return "EmailAnalysis{" +
                "effectivenessScore=" + effectivenessScore +
                ", tone='" + tone + '\'' +
                ", wordCount=" + wordCount +
                ", hasCallToAction=" + hasCallToAction +
                ", personalizationLevel='" + personalizationLevel + '\'' +
                ", strengths='" + strengths + '\'' +
                ", weaknesses='" + weaknesses + '\'' +
                '}';
    }
}

