package com.complai.coldsales.models.guardrails;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Personal data guardrail output.
 * Result from checking if email contains personal/sensitive data.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PersonalDataCheck {
    
    @JsonProperty("is_safe")
    private boolean isSafe;
    
    @JsonProperty("reason")
    private String reason;
    
    @JsonProperty("confidence")
    private double confidence;
    
    @JsonProperty("contains_personal_names")
    private boolean containsPersonalNames;
    
    @JsonProperty("contains_sensitive_data")
    private boolean containsSensitiveData;
    
    @JsonProperty("data_types_found")
    private List<String> dataTypesFound;
    
    @Override
    public String toString() {
        return "PersonalDataCheck{" +
                "isSafe=" + isSafe +
                ", reason='" + reason + '\'' +
                ", confidence=" + confidence +
                ", containsPersonalNames=" + containsPersonalNames +
                ", containsSensitiveData=" + containsSensitiveData +
                ", dataTypesFound=" + (dataTypesFound != null ? dataTypesFound.size() + " types" : "null") +
                '}';
    }
}

