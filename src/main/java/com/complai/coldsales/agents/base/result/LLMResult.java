package com.complai.coldsales.agents.base.result;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;

import java.util.Collections;
import java.util.List;

/**
 * Result object from agent execution with metadata.
 */
@Data
@Builder
@AllArgsConstructor
@ToString
public class LLMResult {
    private Object finalOutput;
    private String model;
    private int tokensUsed;
    private boolean isStructured;
    private int toolCallsMade;
    private List<String> toolsUsed;

    public LLMResult(Object output) {
        this.finalOutput = output;
        this.model = "";
        this.tokensUsed = 0;
        this.isStructured = false;
        this.toolCallsMade = 0;
        this.toolsUsed = Collections.emptyList();
    }
}

