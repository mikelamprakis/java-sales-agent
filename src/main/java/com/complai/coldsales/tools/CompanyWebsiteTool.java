package com.complai.coldsales.tools;

import com.complai.coldsales.agents.base.core.Agent;
import com.complai.coldsales.agents.base.tools.ServiceBackedAgentTool;
import com.complai.coldsales.services.WebScraperService;
import com.complai.coldsales.utils.ServiceError;
import com.complai.coldsales.utils.ToolPromptBuilder;

import java.util.concurrent.CompletableFuture;

/**
 * Service-backed tool that enriches prompts with real website data before delegating to the LLM agent.
 */
public class CompanyWebsiteTool extends ServiceBackedAgentTool {

    private final WebScraperService webScraper;

    public CompanyWebsiteTool(Agent agent, WebScraperService webScraper) {
        super(
                "analyze_company_website",
                "Analyze the company's website to extract business information, products/services, technology stack, and company culture. Uses REAL web scraping to fetch actual website data.",
                agent,
                webScraper
        );
        this.webScraper = webScraper;
    }

    @Override
    protected CompletableFuture<String> buildAugmentedPrompt(String prompt) {
        ToolPromptBuilder builder = ToolPromptBuilder.from(prompt).withExtractedCompanyName();
        
        return webScraper.findCompanyWebsite(builder.getCompanyName())
                .thenCompose(urlResult -> {
                    if (urlResult.isErr()) {
                        ServiceError error = urlResult.unwrapErr();
                        System.err.println(error.format());
                        return builder.buildFallbackPrompt(error.getUserMessage());
                    }

                    String url = urlResult.unwrap();
                    return webScraper.fetchWebsite(url)
                            .thenApply(fetchResult -> {
                                if (fetchResult.isErr()) {
                                    ServiceError error = fetchResult.unwrapErr();
                                    System.err.println(error.format());
                                    return builder.buildFallbackPrompt(error.getUserMessage()).join();
                                }
                                WebScraperService.WebsiteContent content = fetchResult.unwrap();
                                return builder.wrapServiceData(
                                        CompletableFuture.completedFuture(content),
                                        WebScraperService.WebsiteContent::toAnalysisPrompt,
                                        "company website data"
                                ).join();
                            });
                });
    }
}

