package com.complai.coldsales.pipelines;

import com.complai.coldsales.agents.base.core.Agent;
import com.complai.coldsales.agents.base.client.LLMClient;
import com.complai.coldsales.agents.base.result.LLMResult;
import com.complai.coldsales.utils.Utils;
import com.complai.coldsales.managers.reporting.*;
import com.complai.coldsales.models.structured.EmailAnalysis;
import com.complai.coldsales.models.structured.SalesEmail;
import com.complai.coldsales.models.pipeline.email.*;
import com.complai.coldsales.services.EmailService;
import com.complai.coldsales.utils.ExtractorUtils;
import lombok.AllArgsConstructor;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.complai.coldsales.utils.ExtractorUtils.extractSalesEmail;
import static com.complai.coldsales.utils.ExtractorUtils.extractSubjectData;

/**
 * Encapsulates the cold email generation pipeline (Stages 1-5).
 */
@AllArgsConstructor
public class EmailPipeline {
    
    private final LLMClient llmClient;
    private final Agent professionalAgent;
    private final Agent engagingAgent;
    private final Agent busyAgent;
    private final Agent emailAnalyzer;
    private final Agent subjectWriter;
    private final Agent htmlConverter;
    private final EmailService emailService;
    
    public CompletableFuture<EmailPipelineResult> run(String message) {
        return stage1GenerateEmails(message)
                .thenCompose(this::stage2AnalyzeEmails)
                .thenCompose(this::stage3SelectBest)
                .thenCompose(this::stage4GenerateSubject)
                .thenCompose(this::stage5ConvertAndSend)
                .thenApply(EmailPipelineResult::toTypedResult);
    }
    
    // Contexts - using class-based models from models/pipeline/email/EmailPipelineContexts
    private CompletableFuture<EmailPipelineContexts.EmailsCtx> stage1GenerateEmails(String message){
        return new EmailPipelineStep1Reporter()
                .runAsync(() -> generateStructuredEmails(message))
                .thenApply(EmailPipelineContexts.EmailsCtx::new);
    }
    
    private CompletableFuture<EmailPipelineContexts.AnalysesCtx> stage2AnalyzeEmails(EmailPipelineContexts.EmailsCtx ctx){
        return new EmailPipelineStep2Reporter().withEmailCount(ctx.getEmails().size())
                .runAsync(() -> analyzeEmails(ctx.getEmails()))
                .thenApply(analyses -> new EmailPipelineContexts.AnalysesCtx(ctx.getEmails(), analyses));
    }
    
    private CompletableFuture<EmailPipelineContexts.BestCtx> stage3SelectBest(EmailPipelineContexts.AnalysesCtx ctx){
        var best = new EmailPipelineStep3Reporter()
                .run(() -> selectBestStructuredEmail(ctx.getEmails(), ctx.getAnalyses()));
        return CompletableFuture.completedFuture(new EmailPipelineContexts.BestCtx(best.getKey(), best.getValue()));
    }
    
    private CompletableFuture<EmailPipelineContexts.SubjectCtx> stage4GenerateSubject(EmailPipelineContexts.BestCtx ctx){
        return new EmailPipelineStep4Reporter()
                .runAsync(() -> {
                    String body = ctx.getBestEmail().getBody();
                    if (body == null || body.isBlank()) body = "Email body not available";
                    return llmClient.run(subjectWriter, body)
                            .thenApply(r -> extractSubjectData(r.getFinalOutput()));
                })
                .thenApply(subject -> new EmailPipelineContexts.SubjectCtx(ctx.getBestEmail(), ctx.getBestAnalysis(), subject));
    }

    
    private CompletableFuture<EmailPipelineContexts.SentCtx> stage5ConvertAndSend(EmailPipelineContexts.SubjectCtx ctx){
        return new EmailPipelineStep5Reporter()
                .runAsync(() -> {
                    String body = ctx.getBestEmail().getBody();
                    if (body == null || body.isBlank()) body = "Email body not available";
                    return llmClient.run(htmlConverter, body)
                            .thenApply(r -> r.getFinalOutput().toString())
                            .thenApply(html -> emailService.sendHtmlEmail(html, ctx.getSubject().getPrimarySubject()));
                })
                .thenApply(result -> new EmailPipelineContexts.SentCtx(ctx.getBestEmail(), ctx.getBestAnalysis(), ctx.getSubject(), result));
    }

    // Helper Methods
    private CompletableFuture<List<SalesEmail>> generateStructuredEmails(String message) {
        return Utils.trace("Structured email generation", () -> {
            CompletableFuture<LLMResult> professionalFuture = llmClient.run(professionalAgent, message);
            CompletableFuture<LLMResult> engagingFuture = llmClient.run(engagingAgent, message);
            CompletableFuture<LLMResult> busyFuture = llmClient.run(busyAgent, message);
            return CompletableFuture.allOf(professionalFuture, engagingFuture, busyFuture)
                    .thenApply(v -> {
                        List<SalesEmail> emails = new ArrayList<>();
                        emails.add(extractSalesEmail(professionalFuture.join()));
                        emails.add(extractSalesEmail(engagingFuture.join()));
                        emails.add(extractSalesEmail(busyFuture.join()));
                        return emails;
                    });
        });
    }
    
    private CompletableFuture<List<EmailAnalysis>> analyzeEmails(List<SalesEmail> emails) {
        return Utils.trace("Email analysis", () -> {
            List<CompletableFuture<LLMResult>> futures = emails.stream()
                    .map(email -> {
                        String body = email.getBody();
                        if (body == null || body.isBlank()) body = "Email body not available for analysis";

                        return llmClient.run(emailAnalyzer, body);
                    })
                    .toList();
            return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                    .thenApply(v -> futures.stream()
                            .map(future -> ExtractorUtils.extractEmailAnalysis(future.join()))
                            .collect(Collectors.toList()));
        });
    }
    
    private Map.Entry<SalesEmail, EmailAnalysis> selectBestStructuredEmail(List<SalesEmail> emails, List<EmailAnalysis> analyses) {
        List<Map.Entry<Double, Integer>> scores = new ArrayList<>();
        for (int i = 0; i < emails.size(); i++) {
            SalesEmail email = emails.get(i);
            EmailAnalysis analysis = analyses.get(i);
            double score = Stream.of(
                            Map.entry(analysis.getEffectivenessScore(), 0.4),
                            Map.entry(email.getExpectedResponseRate(), 0.3),
                            Map.entry(getPersonalizationScore(analysis.getPersonalizationLevel()), 0.2),
                            Map.entry(analysis.isHasCallToAction() ? 10.0 : 0.0, 0.1)
                    ).mapToDouble(e -> e.getKey().doubleValue() * e.getValue())
                    .sum();
            scores.add(new AbstractMap.SimpleEntry<>(score, i));
        }
        int bestIdx = scores.stream()
                .max(Comparator.comparingDouble(Map.Entry::getKey))
                .map(Map.Entry::getValue)
                .orElse(0);
        return new AbstractMap.SimpleEntry<>(emails.get(bestIdx), analyses.get(bestIdx));
    }
    
    private double getPersonalizationScore(String level) {
        if (level == null || level.isBlank()) return 4.0;
        return switch (level.trim().toLowerCase()) {
            case "high" -> 10.0;
            case "medium" -> 7.0;
            default -> 4.0;
        };
    }
}

