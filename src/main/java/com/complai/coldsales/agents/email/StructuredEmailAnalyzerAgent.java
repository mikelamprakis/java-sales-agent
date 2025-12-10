package com.complai.coldsales.agents.email;

import com.complai.coldsales.agents.base.core.AIAgentComponent;
import com.complai.coldsales.agents.base.guardrails.GuardrailFunction;
import com.complai.coldsales.models.structured.EmailAnalysis;
import lombok.Getter;
import java.util.List;

//Agent that analyzes and evaluates email effectiveness.
@Getter
public class StructuredEmailAnalyzerAgent extends AIAgentComponent {
    private final List<GuardrailFunction> guardrails;

    public StructuredEmailAnalyzerAgent(String model, List<GuardrailFunction> guardrails) {
        super(model);
        this.guardrails = guardrails;
    }

    @Override
    protected String getAgentName() {
        return "Email Analyzer";
    }

    @Override
    protected String getPromptId() {
        return "email/email-analyzer-agent";
    }

    @Override
    protected Class<?> getOutputType() {
        return EmailAnalysis.class;
    }

    @Override
    protected List<GuardrailFunction> getGuardrails() {
        return guardrails;
    }
}
