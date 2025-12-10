package com.complai.coldsales.models.structured;

import com.complai.coldsales.models.types.EmailTone;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Structured output for sales email generation.
 * This is what the LLM agent returns when generating a sales email.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SalesEmail {
    
    @JsonProperty("subject")
    private String subject;
    
    @JsonProperty("body")
    private String body;
    
    @JsonProperty("tone")
    private String tone;
    
    @JsonProperty("call_to_action")
    private String callToAction;
    
    @JsonProperty("personalization_notes")
    private String personalizationNotes;
    
    @JsonProperty("expected_response_rate")
    private int expectedResponseRate;

    public EmailTone getToneEnum() {
        return EmailTone.fromString(tone);
    }

    public static SalesEmail fallback(Object output){
        System.out.println("⚠️  Non-structured output received, creating fallback SalesEmail");
        return SalesEmail.builder()
                .subject("Generated Email")
                .body(output.toString())
                .tone("professional")
                .callToAction("Please reply if interested")
                .personalizationNotes("Generic email")
                .expectedResponseRate(10)
                .build();
    }
    
    @Override
    public String toString() {
        return "SalesEmail{" +
                "subject='" + subject + '\'' +
                ", body='" + (body != null ? body.substring(0, Math.min(100, body.length())) + "..." : "null") + '\'' +
                ", tone='" + tone + '\'' +
                ", callToAction='" + callToAction + '\'' +
                ", expectedResponseRate=" + expectedResponseRate +
                '}';
    }
}

