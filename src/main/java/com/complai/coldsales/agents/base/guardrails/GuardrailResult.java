package com.complai.coldsales.agents.base.guardrails;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * Result from a guardrail check.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GuardrailResult {
    
    private Map<String, Object> outputInfo;
    private boolean tripwireTriggered;

    public GuardrailResult(boolean tripwireTriggered) {
        this.tripwireTriggered = tripwireTriggered;
        this.outputInfo = new HashMap<>();
    }

    public static GuardrailResult pass() {
        return new GuardrailResult(false);
    }

    public static GuardrailResult block(Map<String, Object> info) {
        return GuardrailResult.builder()
                .tripwireTriggered(true)
                .outputInfo(info)
                .build();
    }
}

