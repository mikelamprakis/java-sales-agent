package com.complai.coldsales.agents.research.analyzer;

import com.complai.coldsales.agents.base.core.AIAgentComponent;
import com.complai.coldsales.agents.base.tools.ServiceBackedAgentTool;
import com.complai.coldsales.tools.CompanyWebsiteTool;
import com.complai.coldsales.services.WebScraperService;
import lombok.Getter;

/**
 * Analyzes company website for key information.
 * Now uses REAL web scraping to fetch actual website data!
 * Service work: uses WebScraperService to locate the URL and fetch real page content.
 * LLM work: passes that scraped content to its Agent via LLMClient to interpret and summarize the site.
 */
@Getter
public class CompanyWebsiteAnalyzerAgent extends AIAgentComponent {
    private final WebScraperService webScraperService;

    public CompanyWebsiteAnalyzerAgent(String model, WebScraperService webScraperService) {
        super(model);
        this.webScraperService = webScraperService;
    }

    /**
     * Create a tool that fetches real website data before analysis.
     */
    @Override
    public ServiceBackedAgentTool getServiceBackedAgentTool() {
        return new CompanyWebsiteTool(agent, webScraperService);
    }

    @Override
    protected String getAgentName() {
        return "Company Website Analyzer";
    }

    @Override
    protected String getPromptId() {
        return "research/analyzers/company-website-analyzer-agent";
    }
}
