package com.complai.coldsales.models.pipeline.email;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.complai.coldsales.models.result.PipelineResult;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

/**
 * Represents the complete result of the email generation pipeline.
 * Contains selected email, analysis, subject options, and sending result.
 */
@Getter
@AllArgsConstructor
@ToString
public class EmailPipelineResult implements PipelineResult {
    private final String status;
    private final SelectedEmail selectedEmail;
    private final PipelineAnalysis analysis;
    private final SubjectOptions subjectOptions;
    private final Map<String, String> emailResult;

    public static EmailPipelineResult toTypedResult(EmailPipelineContexts.SentCtx ctx){
        String status = "success".equals(ctx.getEmailResult().get("status")) ? "success" : "error";
        var selectedEmail = new SelectedEmail(
                ctx.getSubject().getPrimarySubject(),
                ctx.getBestEmail().getBody(),
                ctx.getBestEmail().getTone(),
                ctx.getBestEmail().getExpectedResponseRate()
        );
        var analysis = new PipelineAnalysis(
                ctx.getBestAnalysis().getEffectivenessScore(),
                ctx.getBestAnalysis().getPersonalizationLevel(),
                ctx.getBestAnalysis().getStrengths(),
                ctx.getBestAnalysis().getImprovementSuggestions()
        );
        var subjectOptions = new SubjectOptions(
                ctx.getSubject().getPrimarySubject(),
                ctx.getSubject().getAlternativeSubjects(),
                ctx.getSubject().getPredictedOpenRate(),
                ctx.getSubject().getSubjectType()
        );
        return new EmailPipelineResult(status, selectedEmail, analysis, subjectOptions, ctx.getEmailResult());
    }

    public Map<String, Object> toMap() {
        Map<String, Object> root = new HashMap<>();
        root.put("status", status);
        
        if (selectedEmail != null) {
            Map<String, Object> sel = new HashMap<>();
            sel.put("subject", nz(selectedEmail.getSubject()));
            sel.put("body", nz(selectedEmail.getBody()));
            sel.put("tone", nz(selectedEmail.getTone()));
            sel.put("expected_response_rate", selectedEmail.getExpectedResponseRate());
            root.put("selected_email", sel);
        }
        
        if (analysis != null) {
            Map<String, Object> an = new HashMap<>();
            an.put("effectiveness_score", analysis.getEffectivenessScore());
            an.put("personalization_level", nz(analysis.getPersonalizationLevel()));
            an.put("strengths", nz(analysis.getStrengths()));
            an.put("improvement_suggestions", nz(analysis.getImprovementSuggestions()));
            root.put("analysis", an);
        }
        
        if (subjectOptions != null) {
            Map<String, Object> so = new HashMap<>();
            so.put("primary", nz(subjectOptions.getPrimary()));
            so.put("alternatives", subjectOptions.getAlternatives() != null ? subjectOptions.getAlternatives() : List.of());
            so.put("predicted_open_rate", subjectOptions.getPredictedOpenRate());
            so.put("strategy", nz(subjectOptions.getStrategy()));
            root.put("subject_options", so);
        }
        
        if (emailResult != null) {
            root.put("email_result", emailResult);
        }
        
        return root;
    }
    
    private static String nz(String s) {
        return s == null ? "" : s;
    }

}

