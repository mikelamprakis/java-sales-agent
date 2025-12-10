package com.complai.coldsales.models;

import com.complai.coldsales.models.result.*;
import com.complai.coldsales.models.structured.*;
import com.complai.coldsales.models.types.EmailTone;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for model classes.
 */
class ModelsTest {
    
    @Test
    void testSalesEmail() {
        SalesEmail email = new SalesEmail();
        email.setBody("Test email body");
        email.setTone("professional");
        email.setExpectedResponseRate(15);
        
        assertEquals("Test email body", email.getBody());
        assertEquals("professional", email.getTone());
        assertEquals(15, email.getExpectedResponseRate());
    }
    
    @Test
    void testSalesEmailFallback() {
        SalesEmail email = SalesEmail.fallback("Raw text output");
        
        assertNotNull(email);
        assertNotNull(email.getBody());
    }
    
    @Test
    void testEmailAnalysis() {
        EmailAnalysis analysis = new EmailAnalysis();
        analysis.setEffectivenessScore(8);
        analysis.setPersonalizationLevel("high");
        analysis.setStrengths("Clear CTA, Personalized");
        analysis.setImprovementSuggestions("Add more urgency");
        analysis.setHasCallToAction(true);
        
        assertEquals(8, analysis.getEffectivenessScore());
        assertEquals("high", analysis.getPersonalizationLevel());
        assertNotNull(analysis.getStrengths());
        assertTrue(analysis.isHasCallToAction());
    }
    
    @Test
    void testEmailAnalysisFallback() {
        EmailAnalysis analysis = EmailAnalysis.fallback("Raw analysis");
        
        assertNotNull(analysis);
    }
    
    @Test
    void testEmailSubject() {
        EmailSubject subject = new EmailSubject();
        subject.setPrimarySubject("Test Subject");
        subject.setAlternativeSubjects(List.of("Alt 1", "Alt 2"));
        subject.setPredictedOpenRate(25);
        subject.setSubjectType("question");
        
        assertEquals("Test Subject", subject.getPrimarySubject());
        assertEquals(2, subject.getAlternativeSubjects().size());
        assertEquals(25, subject.getPredictedOpenRate());
        assertEquals("question", subject.getSubjectType());
    }
    
    @Test
    void testEmailSubjectFallback() {
        EmailSubject subject = EmailSubject.fallback("Raw subject");
        
        assertNotNull(subject);
    }
    
    @Test
    void testProspectResearch() {
        ProspectResearch research = new ProspectResearch();
        research.setCompanyOverview("Test company overview");
        research.setCompanySize("200-500 employees");
        research.setIndustry("Technology");
        research.setKeyPainPoints(List.of("Compliance", "Security"));
        research.setPersonalizationOpportunities(List.of("Recent funding", "New product"));
        research.setRecommendedApproach("Focus on compliance automation");
        research.setRecentHooks(List.of("Raised Series B", "Launched new product"));
        
        assertEquals("Test company overview", research.getCompanyOverview());
        assertEquals("200-500 employees", research.getCompanySize());
        assertEquals(2, research.getKeyPainPoints().size());
        assertEquals(2, research.getPersonalizationOpportunities().size());
    }
    
    @Test
    void testProspectResearchFallback() {
        ProspectResearch research = ProspectResearch.fallback("Raw research data");
        
        assertNotNull(research);
    }
    
    @Test
    void testEmailResult() {
        com.complai.coldsales.models.pipeline.email.EmailPipelineResult pipelineResult = 
                new com.complai.coldsales.models.pipeline.email.EmailPipelineResult(
                        "success",
                        new com.complai.coldsales.models.pipeline.email.SelectedEmail(
                                "Subject", "Body", "professional", 15
                        ),
                        new com.complai.coldsales.models.pipeline.email.PipelineAnalysis(
                                8, "high", "Clear CTA", "Add more urgency"
                        ),
                        new com.complai.coldsales.models.pipeline.email.SubjectOptions(
                                "Subject", List.of(), 25, "question"
                        ),
                        Map.of("status", "success")
                );
        
        EmailResult result = new EmailResult(pipelineResult);
        
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertNotNull(result.getEmailPipelineResult());
    }
    
    @Test
    void testHybridResult() {
        com.complai.coldsales.models.pipeline.hybrid.EmailHybridResult hybrid = 
                new com.complai.coldsales.models.pipeline.hybrid.EmailHybridResult(
                        new com.complai.coldsales.models.pipeline.email.EmailPipelineResult(
                                "success",
                                new com.complai.coldsales.models.pipeline.email.SelectedEmail(
                                        "Subject", "Body", "professional", 15
                                ),
                                new com.complai.coldsales.models.pipeline.email.PipelineAnalysis(
                                        8, "high", "Clear CTA", "Add more urgency"
                                ),
                                new com.complai.coldsales.models.pipeline.email.SubjectOptions(
                                        "Subject", List.of(), 25, "question"
                                ),
                                Map.of("status", "success")
                        ),
                        new com.complai.coldsales.models.pipeline.hybrid.ResearchPhase(
                                2, List.of("tool1", "tool2"), "Research summary"
                        ),
                        new com.complai.coldsales.models.pipeline.hybrid.EmailPhase(
                                "manual-orchestration", List.of("step1", "step2")
                        )
                );
        
        HybridResult result = new HybridResult(hybrid);
        
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertNotNull(result.getHybridResult());
        assertEquals(2, result.getResearchToolsUsed());
    }
    
    @Test
    void testErrorResult() {
        Exception exception = new RuntimeException("Test error");
        ErrorResult result = new ErrorResult("Test error message", exception);
        
        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertEquals("Test error message", result.getMessage());
        assertEquals(exception, result.getCause());
    }
    
    @Test
    void testEmailTone() {
        assertEquals(EmailTone.PROFESSIONAL, EmailTone.valueOf("PROFESSIONAL"));
        assertEquals(EmailTone.ENGAGING, EmailTone.valueOf("ENGAGING"));
        assertEquals(EmailTone.CASUAL, EmailTone.valueOf("CASUAL"));
    }
}

