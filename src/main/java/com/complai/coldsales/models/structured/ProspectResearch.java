package com.complai.coldsales.models.structured;

import com.complai.coldsales.utils.ExtractorUtils;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;

/**
 * Structured output for prospect research (agent-of-agents pattern).
 * This is what the LLM agent returns when researching a prospect.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProspectResearch {
    
    @JsonProperty("company_overview")
    private String companyOverview;
    
    @JsonProperty("company_size")
    private String companySize;
    
    @JsonProperty("industry")
    private String industry;
    
    @JsonProperty("key_pain_points")
    private List<String> keyPainPoints;
    
    @JsonProperty("personalization_opportunities")
    private List<String> personalizationOpportunities;
    
    @JsonProperty("recommended_approach")
    private String recommendedApproach;
    
    @JsonProperty("recent_hooks")
    private List<String> recentHooks;
    
    @JsonProperty("confidence_level")
    private String confidenceLevel; // "high", "medium", "low"
    
    public static ProspectResearch fallback(String rawOutput) {
        System.out.println("⚠️  Non-structured research output received, creating fallback ProspectResearch");
        return ProspectResearch.builder()
                .companyOverview(rawOutput)
                .companySize("Unknown")
                .industry("Unknown")
                .keyPainPoints(List.of("Not specified"))
                .personalizationOpportunities(List.of("General personalization"))
                .recommendedApproach("Use general SOC2 positioning")
                .recentHooks(List.of())
                .confidenceLevel("low")
                .build();
    }

    public static String buildProspectResearchSummary(String companyName, Object data) {
        ProspectResearch r = ExtractorUtils.extractProspectResearch(data);
        System.out.println(r);
        StringBuilder sb = new StringBuilder();
        sb.append("Company: ").append(companyName).append("\n");
        sb.append("Overview: ").append(r.getCompanyOverview() != null ? r.getCompanyOverview() : "Not available").append("\n");
        sb.append("Size: ").append(r.getCompanySize() != null ? r.getCompanySize() : "Unknown").append("\n");
        sb.append("Industry: ").append(r.getIndustry() != null ? r.getIndustry() : "Unknown").append("\n\n");

        sb.append("Key Pain Points:\n");
        if (r.getKeyPainPoints() != null && !r.getKeyPainPoints().isEmpty()) {
            for (String pain : r.getKeyPainPoints()) {
                sb.append("- ").append(pain).append("\n");
            }
        } else {
            sb.append("- Not specified\n");
        }

        sb.append("\nPersonalization Hooks:\n");
        if (r.getPersonalizationOpportunities() != null && !r.getPersonalizationOpportunities().isEmpty()) {
            for (String hook : r.getPersonalizationOpportunities()) {
                sb.append("- ").append(hook).append("\n");
            }
        } else {
            sb.append("- General personalization\n");
        }

        sb.append("\nRecommended Approach: ").append(r.getRecommendedApproach() != null ? r.getRecommendedApproach() : "Use general SOC2 positioning");

        if (r.getRecentHooks() != null && !r.getRecentHooks().isEmpty()) {
            sb.append("\n\nRecent News:\n");
            for (String hook : r.getRecentHooks()) {
                sb.append("- ").append(hook).append("\n");
            }
        }
        return sb.toString();
    }
    
    @Override
    public String toString() {
        return "ProspectResearch{" +
                "companyOverview='" + (companyOverview != null ? companyOverview.substring(0, Math.min(100, companyOverview.length())) + "..." : "null") + '\'' +
                ", companySize='" + companySize + '\'' +
                ", industry='" + industry + '\'' +
                ", keyPainPoints=" + (keyPainPoints != null ? keyPainPoints.size() + " items" : "null") +
                ", personalizationOpportunities=" + (personalizationOpportunities != null ? personalizationOpportunities.size() + " items" : "null") +
                ", confidenceLevel='" + confidenceLevel + '\'' +
                '}';
    }
}

