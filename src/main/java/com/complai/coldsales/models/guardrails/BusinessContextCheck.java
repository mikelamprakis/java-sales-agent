package com.complai.coldsales.models.guardrails;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Business context guardrail output.
 * Result from checking if email aligns with business context and brand.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BusinessContextCheck {
    
    @JsonProperty("is_safe")
    private boolean isSafe;
    
    @JsonProperty("reason")
    private String reason;
    
    @JsonProperty("confidence")
    private double confidence;
    
    @JsonProperty("mentions_competitors")
    private boolean mentionsCompetitors;
    
    @JsonProperty("off_brand_messaging")
    private boolean offBrandMessaging;
    
    @JsonProperty("compliance_issues")
    private List<String> complianceIssues;
    
    @Override
    public String toString() {
        return "BusinessContextCheck{" +
                "isSafe=" + isSafe +
                ", reason='" + reason + '\'' +
                ", confidence=" + confidence +
                ", mentionsCompetitors=" + mentionsCompetitors +
                ", offBrandMessaging=" + offBrandMessaging +
                ", complianceIssues=" + (complianceIssues != null ? complianceIssues.size() + " issues" : "null") +
                '}';
    }
}

