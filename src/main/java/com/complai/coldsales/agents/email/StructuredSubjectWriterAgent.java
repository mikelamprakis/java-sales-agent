package com.complai.coldsales.agents.email;

import com.complai.coldsales.agents.base.core.AIAgentComponent;
import com.complai.coldsales.agents.base.guardrails.GuardrailFunction;
import com.complai.coldsales.models.structured.EmailSubject;
import lombok.Getter;

import java.util.List;

// Agent that generates structured email subjects with alternatives.
@Getter
public class StructuredSubjectWriterAgent extends AIAgentComponent {
    private final List<GuardrailFunction> guardrails;

    public StructuredSubjectWriterAgent(String model, List<GuardrailFunction> guardrails) {
        super(model);
        this.guardrails = guardrails;
    }

    @Override
    protected String getAgentName() {
        return "Subject Writer (Structured)";
    }

    @Override
    protected String getPromptId() {
        return "email/subject-writer-agent";
    }

    @Override
    protected Class<?> getOutputType() {
        return EmailSubject.class;
    }

    @Override
    protected List<GuardrailFunction> getGuardrails() {
        return guardrails;
    }
}
