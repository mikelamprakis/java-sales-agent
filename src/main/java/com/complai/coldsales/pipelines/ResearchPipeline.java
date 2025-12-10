package com.complai.coldsales.pipelines;

import com.complai.coldsales.agents.base.core.Agent;
import com.complai.coldsales.agents.base.client.LLMClient;
import com.complai.coldsales.managers.reporting.ResearchPipelineStep1Reporter;
import com.complai.coldsales.managers.reporting.ResearchPipelineStep2Reporter;
import com.complai.coldsales.models.structured.ProspectResearch;
import com.complai.coldsales.models.pipeline.research.ResearchRunResult;
import com.complai.coldsales.models.pipeline.research.RunnerSnapshot;
import com.complai.coldsales.utils.ExtractorUtils;
import lombok.AllArgsConstructor;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Encapsulates prospect research execution and summary building.
 */
@AllArgsConstructor
public class ResearchPipeline {

    private final LLMClient llmClient;
    private final Agent prospectResearcher;
    
    public CompletableFuture<ResearchRunResult> run(String companyName, String targetRole) {
        return CompletableFuture.completedFuture(getPromptPrompt(companyName, targetRole))
                .thenCompose(prompt -> stage1FetchResearch(companyName, targetRole, prompt))
                .thenCompose(snapshot -> stage2BuildSummary(companyName, snapshot));
    }

    private String getPromptPrompt(String companyName, String targetRole){
        return "Research " + companyName + " to enable highly personalized cold sales outreach.\n\n" +
                        "Target role: " + targetRole + "\n" +
                        "Our product: ComplAI - SOC2 compliance automation platform\n\n" +
                        "Focus your research on:\n" +
                        "- Company size, industry, and growth stage\n" +
                        "- Pain points related to compliance, audits, or security\n" +
                        "- Recent news or events that create outreach opportunities\n" +
                        "- How SOC2 compliance affects their business\n\n" +
                        "Use the available research tools to gather this information.";
    }

    private CompletableFuture<RunnerSnapshot> stage1FetchResearch(String companyName, String targetRole, String prompt) {
        ResearchPipelineStep1Reporter step1 = new ResearchPipelineStep1Reporter(companyName, targetRole);
        return step1.runAsync(() -> llmClient.run(prospectResearcher, prompt))
                .thenApply(result -> new RunnerSnapshot(result.getFinalOutput(), result.getToolCallsMade(), result.getToolsUsed()));
    }
    
    private CompletableFuture<ResearchRunResult> stage2BuildSummary(String companyName, RunnerSnapshot snapshot) {
        ResearchPipelineStep2Reporter step2 = new ResearchPipelineStep2Reporter(companyName);
        return CompletableFuture.supplyAsync(() ->
                step2.run(() -> ProspectResearch.buildProspectResearchSummary(companyName, snapshot.getFinalOutput())))
                .thenApply(summary -> new ResearchRunResult(snapshot.getFinalOutput(), snapshot.getToolCallsMade(), snapshot.getToolNames(), summary));
    }
}


