package com.complai.coldsales.agents.research.analyzer;

import com.complai.coldsales.agents.base.core.AIAgentComponent;
import lombok.Getter;

/**
 * Analyzes company's competitive landscape.
 * Service work: none. Today—this one is purely LLM-driven (prompt + model) without external scraping.
 * LLM work: generates competitor insights directly from the user prompt/context.
 */
@Getter
public class CompetitorAnalyzerAgent extends AIAgentComponent {
    
    public CompetitorAnalyzerAgent(String model) {
        super(model);
    }

    @Override
    protected String getAgentName() {
        return "Competitor Analyzer";
    }

    @Override
    protected String getPromptId() {
        return "research/analyzers/competitor-analyzer-agent";
    }
}
