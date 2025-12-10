package com.complai.coldsales.agents.research.analyzer;

import com.complai.coldsales.agents.base.core.AIAgentComponent;
import com.complai.coldsales.agents.base.tools.ServiceBackedAgentTool;
import com.complai.coldsales.tools.NewsAndPressTool;
import com.complai.coldsales.services.NewsSearchService;
import lombok.Getter;

/**
 * Finds and analyzes recent news about the company.
 * Now uses REAL web search to find actual news articles!
 * Service work: uses NewsSearch.searchRecentNews (and formatArticlesForAnalysis) to gather recent press coverage.
 * LLM work: hands those formatted articles to its Agent so the LLM can highlight relevant news/hooks.
 */
@Getter
public class NewsAndPressAnalyzerAgent extends AIAgentComponent {
    private final NewsSearchService newsSearchService;

    public NewsAndPressAnalyzerAgent(String model, NewsSearchService newsSearchService) {
        super(model);
        this.newsSearchService = newsSearchService;
    }

    /**
     * Create a tool that searches for real news before analysis.
     */
    @Override
    public ServiceBackedAgentTool getServiceBackedAgentTool() {
        return new NewsAndPressTool(agent, newsSearchService);
    }

    @Override
    protected String getAgentName() {
        return "News and Press Analyzer";
    }

    @Override
    protected String getPromptId() {
        return "research/analyzers/news-and-press-analyzer-agent";
    }
}
