package com.complai.coldsales.models.guardrails;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Content safety guardrail output.
 * Result from checking if email content is safe and appropriate.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContentSafetyCheck {
    
    @JsonProperty("is_safe")
    private boolean isSafe;
    
    @JsonProperty("reason")
    private String reason;
    
    @JsonProperty("confidence")
    private double confidence;
    
    @JsonProperty("contains_spam_indicators")
    private boolean containsSpamIndicators;
    
    @JsonProperty("inappropriate_content")
    private boolean inappropriateContent;
    
    @JsonProperty("policy_violations")
    private List<String> policyViolations;
    
    @Override
    public String toString() {
        return "ContentSafetyCheck{" +
                "isSafe=" + isSafe +
                ", reason='" + reason + '\'' +
                ", confidence=" + confidence +
                ", containsSpamIndicators=" + containsSpamIndicators +
                ", inappropriateContent=" + inappropriateContent +
                ", policyViolations=" + (policyViolations != null ? policyViolations.size() + " violations" : "null") +
                '}';
    }
}

