package com.complai.coldsales.models.pipeline.email;

import com.complai.coldsales.models.structured.EmailAnalysis;
import com.complai.coldsales.models.structured.EmailSubject;
import com.complai.coldsales.models.structured.SalesEmail;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import java.util.List;
import java.util.Map;

/**
 * Context classes used internally in the email pipeline.
 * These represent the state at different stages of pipeline execution.
 */
public class EmailPipelineContexts {

    @AllArgsConstructor
    @Getter
    @ToString
    public static class EmailsCtx {
        private final List<SalesEmail> emails;
    }
    
    @AllArgsConstructor
    @Getter
    @ToString
    public static class AnalysesCtx {
        private final List<SalesEmail> emails;
        private final List<EmailAnalysis> analyses;
    }

    @AllArgsConstructor
    @Getter
    @ToString
    public static class BestCtx {
        private final SalesEmail bestEmail;
        private final EmailAnalysis bestAnalysis;
    }

    @AllArgsConstructor
    @Getter
    @ToString
    public static class SubjectCtx {
        private final SalesEmail bestEmail;
        private final EmailAnalysis bestAnalysis;
        private final EmailSubject subject;
    }

    @AllArgsConstructor
    @Getter
    @ToString
    public static class SentCtx {
        private final SalesEmail bestEmail;
        private final EmailAnalysis bestAnalysis;
        private final EmailSubject subject;
        private final Map<String, String> emailResult;
    }
}

