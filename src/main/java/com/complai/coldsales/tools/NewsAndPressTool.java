package com.complai.coldsales.tools;

import com.complai.coldsales.agents.base.core.Agent;
import com.complai.coldsales.agents.base.tools.ServiceBackedAgentTool;
import com.complai.coldsales.services.NewsSearchService;
import com.complai.coldsales.utils.ToolPromptBuilder;

import java.util.concurrent.CompletableFuture;

/**
 * Service-backed tool that fetches recent news articles before deferring to the LLM agent.
 */
public class NewsAndPressTool extends ServiceBackedAgentTool {

    private final NewsSearchService newsSearch;

    public NewsAndPressTool(Agent agent, NewsSearchService newsSearch) {
        super(
                "analyze_recent_news",
                "Search for and analyze recent news about the company including funding, product launches, expansions, or challenges. Uses REAL web search to find actual news articles.",
                agent,
                newsSearch
        );
        this.newsSearch = newsSearch;
    }

    @Override
    protected CompletableFuture<String> buildAugmentedPrompt(String prompt) {
        ToolPromptBuilder builder = ToolPromptBuilder.from(prompt).withExtractedCompanyName();
        return builder.wrapServiceDataResultWithCustomInstruction(
                newsSearch.searchRecentNews(builder.getCompanyName()),
                newsSearch::formatArticlesForAnalysis,
                "Analyze these recent news articles about {companyName}:\n\n{data}"
        );
    }
}

