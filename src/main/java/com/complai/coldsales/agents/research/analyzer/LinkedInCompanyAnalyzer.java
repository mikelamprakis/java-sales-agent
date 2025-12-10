package com.complai.coldsales.agents.research.analyzer;

import com.complai.coldsales.agents.base.core.AIAgentComponent;
import com.complai.coldsales.agents.base.tools.ServiceBackedAgentTool;
import com.complai.coldsales.tools.LinkedInCompanyTool;
import com.complai.coldsales.services.LinkedInScraperService;
import lombok.Getter;

/**
 * Extracts company information from LinkedIn.
 * Now attempts REAL LinkedIn scraping (with fallback due to ToS restrictions).
 */
@Getter
public class LinkedInCompanyAnalyzer extends AIAgentComponent {
    private final LinkedInScraperService linkedInScraperService;

    public LinkedInCompanyAnalyzer(String model, LinkedInScraperService linkedInScraperService) {
        super(model);
        this.linkedInScraperService = linkedInScraperService;
    }

    /**
     * Create a tool that attempts real LinkedIn scraping before analysis.
     */
    @Override
    public ServiceBackedAgentTool getServiceBackedAgentTool() {
        return new LinkedInCompanyTool(agent, linkedInScraperService);
    }

    @Override
    protected String getAgentName() {
        return "LinkedIn Company Analyzer";
    }

    @Override
    protected String getPromptId() {
        return "research/analyzers/linkedin-company-analyzer";
    }
}
