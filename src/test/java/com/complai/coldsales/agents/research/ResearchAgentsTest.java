package com.complai.coldsales.agents.research;

import com.complai.coldsales.TestUtils;
import com.complai.coldsales.agents.base.core.Agent;
import com.complai.coldsales.agents.base.tools.AgentTool;
import com.complai.coldsales.agents.base.client.LLMClient;
import com.complai.coldsales.agents.research.analyzer.*;
import com.complai.coldsales.config.Settings;
import com.complai.coldsales.services.LinkedInScraperService;
import com.complai.coldsales.services.NewsSearchService;
import com.complai.coldsales.services.ServicesRegistry;
import com.complai.coldsales.services.WebScraperService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for research agents.
 */
class ResearchAgentsTest {
    
    private LLMClient llmClient;
    private Settings settings;
    private WebScraperService webScraperService;
    private LinkedInScraperService linkedInScraperService;
    private NewsSearchService newsSearchService;
    private ServicesRegistry servicesRegistry;
    
    @BeforeEach
    void setUp() {
        settings = TestUtils.createTestSettings();
        llmClient = LLMClient.fromSettings(settings);
        webScraperService = new WebScraperService();
        linkedInScraperService = new LinkedInScraperService();
        newsSearchService = new NewsSearchService();
        servicesRegistry = ServicesRegistry.builder()
                .settings(settings)
                .webScraperService(webScraperService)
                .linkedInScraperService(linkedInScraperService)
                .newsSearchService(newsSearchService)
                .build();
    }

    @AfterEach
    void tearDown() {
        servicesRegistry.close();
    }
    
    @Test
    void testProspectResearchAgent() {
        ProspectResearchAgent agentWrapper = new ProspectResearchAgent(
                llmClient,
                settings.getModel(),
                servicesRegistry
        );
        Agent agent = agentWrapper.getAgent();
        
        assertNotNull(agent);
        assertEquals("Prospect Research Agent", agent.getName());
        assertTrue(agent.hasTools()); // Should have tools for agent-of-agents pattern
        assertTrue(agent.getTools().size() > 0);
        assertTrue(agent.hasStructuredOutput());
    }
    
    @Test
    void testCompanyWebsiteAnalyzerAgent() {
        CompanyWebsiteAnalyzerAgent agentWrapper = new CompanyWebsiteAnalyzerAgent(settings.getModel(), webScraperService);
        Agent agent = agentWrapper.getAgent();
        
        assertNotNull(agent);
        assertEquals("Company Website Analyzer", agent.getName());
    }
    
    @Test
    void testCompanyWebsiteAnalyzerAgentCreateRealDataTool() {
        CompanyWebsiteAnalyzerAgent agentWrapper = new CompanyWebsiteAnalyzerAgent(settings.getModel(), webScraperService);
        AgentTool tool = agentWrapper.getServiceBackedAgentTool();
        
        assertNotNull(tool);
        assertEquals("analyze_company_website", tool.getName());
        assertNotNull(tool.getDescription());
        assertTrue(tool.getDescription().contains("website"));
    }
    
    @Test
    void testLinkedInCompanyAnalyzer() {
        LinkedInCompanyAnalyzer agentWrapper = new LinkedInCompanyAnalyzer(settings.getModel(), linkedInScraperService);
        Agent agent = agentWrapper.getAgent();
        
        assertNotNull(agent);
        assertTrue(agent.getName().contains("LinkedIn"));
    }
    
    @Test
    void testNewsAndPressAnalyzerAgent() {
        NewsAndPressAnalyzerAgent agentWrapper = new NewsAndPressAnalyzerAgent(settings.getModel(), newsSearchService);
        Agent agent = agentWrapper.getAgent();
        
        assertNotNull(agent);
        assertTrue(agent.getName().contains("News") || agent.getName().contains("Press"));
    }
    
    @Test
    void testCompetitorAnalyzerAgent() {
        CompetitorAnalyzerAgent agentWrapper = new CompetitorAnalyzerAgent(settings.getModel());
        Agent agent = agentWrapper.getAgent();
        
        assertNotNull(agent);
        assertTrue(agent.getName().contains("Competitor") || agent.getName().contains("Competitive"));
    }
}

