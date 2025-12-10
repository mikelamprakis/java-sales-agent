package com.complai.coldsales.tools;

import com.complai.coldsales.agents.base.core.Agent;
import com.complai.coldsales.agents.base.tools.ServiceBackedAgentTool;
import com.complai.coldsales.services.LinkedInScraperService;
import com.complai.coldsales.utils.ToolPromptBuilder;

import java.util.concurrent.CompletableFuture;

/**
 * Service-backed tool that pulls LinkedIn data before asking the LLM to analyze it.
 */
public class LinkedInCompanyTool extends ServiceBackedAgentTool {

    private final LinkedInScraperService linkedInScraper;

    public LinkedInCompanyTool(Agent agent, LinkedInScraperService linkedInScraper) {
        super(
                "analyze_linkedin_profile",
                "Analyze the company's LinkedIn page for employee count, growth trends, recent posts, key executives, and company culture. Attempts REAL LinkedIn scraping (may fallback due to ToS restrictions).",
                agent,
                linkedInScraper
        );
        this.linkedInScraper = linkedInScraper;
    }

    @Override
    protected CompletableFuture<String> buildAugmentedPrompt(String prompt) {
        ToolPromptBuilder builder = ToolPromptBuilder.from(prompt).withExtractedCompanyName();
        return builder.wrapServiceDataResult(
                linkedInScraper.scrapeCompanyPage(builder.getCompanyName()),
                LinkedInScraperService.LinkedInCompanyData::toAnalysisPrompt,
                "LinkedIn company data"
        );
    }
}

