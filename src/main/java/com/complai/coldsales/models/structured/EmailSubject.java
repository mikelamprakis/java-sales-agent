package com.complai.coldsales.models.structured;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Arrays;
import java.util.List;

/**
 * Structured output for email subject generation.
 * This is what the LLM agent returns when generating email subjects.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailSubject {
    
    @JsonProperty("primary_subject")
    private String primarySubject;
    
    @JsonProperty("alternative_subjects")
    private List<String> alternativeSubjects;
    
    @JsonProperty("subject_type")
    private String subjectType; // "question", "benefit", "urgency", "curiosity", "personal"
    
    @JsonProperty("predicted_open_rate")
    private int predictedOpenRate;

    public static EmailSubject fallback(Object output){
        String subjectStr = output.toString();
        return EmailSubject.builder()
                .primarySubject(subjectStr.length() > 100 ? subjectStr.substring(0, 100) + "..." : subjectStr)
                .alternativeSubjects(Arrays.asList(
                        "ComplAI: Streamline Your SOC2 Audit Process",
                        "Cut Your Audit Prep Time by 80% with AI",
                        "From Weeks to Days: Automate Your SOC2 Compliance"
                ))
                .subjectType("benefit")
                .predictedOpenRate(25)
                .build();
    }
    
    @Override
    public String toString() {
        return "EmailSubject{" +
                "primarySubject='" + primarySubject + '\'' +
                ", alternativeSubjects=" + (alternativeSubjects != null ? alternativeSubjects.size() + " options" : "null") +
                ", subjectType='" + subjectType + '\'' +
                ", predictedOpenRate=" + predictedOpenRate +
                '}';
    }
}

