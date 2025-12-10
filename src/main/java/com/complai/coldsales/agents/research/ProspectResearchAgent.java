package com.complai.coldsales.agents.research;

import com.complai.coldsales.agents.base.core.AIAgentComponent;
import com.complai.coldsales.agents.base.tools.AgentTool;
import com.complai.coldsales.agents.base.client.LLMClient;
import com.complai.coldsales.agents.research.analyzer.CompanyWebsiteAnalyzerAgent;
import com.complai.coldsales.agents.research.analyzer.CompetitorAnalyzerAgent;
import com.complai.coldsales.agents.research.analyzer.LinkedInCompanyAnalyzer;
import com.complai.coldsales.agents.research.analyzer.NewsAndPressAnalyzerAgent;
import com.complai.coldsales.services.ServicesRegistry;
import com.complai.coldsales.models.structured.ProspectResearch;
import lombok.Getter;

import java.util.List;

/**
 * Prospect Research Agent - Agent-of-Agents pattern implementation.
 * 
 * This agent demonstrates the agent-of-agents pattern where:
 * - AI decides which research sources to check
 * - AI decides the order of research
 * - AI decides when it has enough information
 * - AI can call the same tool multiple times if needed
 */
@Getter
public class ProspectResearchAgent extends AIAgentComponent {
    
    private final List<AgentTool> tools;

    public ProspectResearchAgent(
            LLMClient llmClient,
            String model,
            ServicesRegistry servicesRegistry
    ) {
        super(model);
        
        // Initialize research tool agents
        CompanyWebsiteAnalyzerAgent websiteAnalyzerAgent = new CompanyWebsiteAnalyzerAgent(model, servicesRegistry.getWebScraperService());
        LinkedInCompanyAnalyzer linkedInAnalyzerAgent = new LinkedInCompanyAnalyzer(model, servicesRegistry.getLinkedInScraperService());
        NewsAndPressAnalyzerAgent newsAnalyzerAgent = new NewsAndPressAnalyzerAgent(model, servicesRegistry.getNewsSearchService());
        CompetitorAnalyzerAgent competitorAnalyzer = new CompetitorAnalyzerAgent(model);
        
        // Convert agents to tools with REAL web scraping - THIS IS WHERE agent-of-agents HAPPENS!
        // These tools now fetch actual data from websites, LinkedIn, and news sources!
        AgentTool websiteAnalyzerAgentTool = websiteAnalyzerAgent.getServiceBackedAgentTool();
        AgentTool linkedInAnalyzerAgentTool = linkedInAnalyzerAgent.getServiceBackedAgentTool();
        AgentTool newsAnalyzerAgentTool = newsAnalyzerAgent.getServiceBackedAgentTool();

        // Competitor analyzer still uses standard tool (can be enhanced later)
        AgentTool competitorAnalyzerTool = competitorAnalyzer.getAgent().asTool(
            "analyze_competitive_position",
            "Analyze the company's competitive landscape and market position. Use this to understand how SOC2 compliance affects their competitive strategy."
        );

        this.tools = List.of(websiteAnalyzerAgentTool, linkedInAnalyzerAgentTool, newsAnalyzerAgentTool, competitorAnalyzerTool);
    }

    @Override
    protected String getAgentName() {
        return "Prospect Research Agent";
    }

    @Override
    protected String getPromptId() {
        return "research/prospect-research-agent";
    }

    @Override
    protected Class<?> getOutputType() {
        return ProspectResearch.class;
    }

    @Override
    protected List<AgentTool> getTools() {
        return tools;
    }
}

