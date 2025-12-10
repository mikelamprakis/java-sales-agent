package com.complai.coldsales.models.result;

import com.complai.coldsales.models.pipeline.email.EmailPipelineResult;
import com.complai.coldsales.models.pipeline.email.PipelineAnalysis;
import com.complai.coldsales.models.pipeline.email.SelectedEmail;
import com.complai.coldsales.models.pipeline.email.SubjectOptions;
import lombok.Getter;

import java.util.Map;

/**
 * Type-safe result wrapper for email pipeline execution.
 * Replaces Map<String, Object> with proper type-safe access.
 */
@Getter
public class EmailResult implements PipelineResult {
    private final EmailPipelineResult emailPipelineResult;
    
    public EmailResult(EmailPipelineResult emailPipelineResult) {
        this.emailPipelineResult = emailPipelineResult;
    }
    
    @Override
    public String getStatus() {
        return emailPipelineResult.getStatus();
    }

    public SelectedEmail getSelectedEmail() {
        return emailPipelineResult.getSelectedEmail();
    }
    
    public PipelineAnalysis getAnalysis() {
        return emailPipelineResult.getAnalysis();
    }
    
    public SubjectOptions getSubjectOptions() {
        return emailPipelineResult.getSubjectOptions();
    }
    
    public Map<String, String> getEmailResult() {
        return emailPipelineResult.getEmailResult();
    }
    
    @Override
    public Map<String, Object> toMap() {
        return emailPipelineResult.toMap();
    }
    
    @Override
    public String toString() {
        return "EmailResult{" +
                "status='" + getStatus() + '\'' +
                ", selectedEmail=" + getSelectedEmail() +
                ", analysis=" + getAnalysis() +
                ", subjectOptions=" + getSubjectOptions() +
                '}';
    }
}

