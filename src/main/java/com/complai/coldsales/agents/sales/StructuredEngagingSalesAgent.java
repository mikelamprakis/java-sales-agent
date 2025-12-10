package com.complai.coldsales.agents.sales;

import com.complai.coldsales.agents.base.core.AIAgentComponent;
import com.complai.coldsales.agents.base.guardrails.GuardrailFunction;
import com.complai.coldsales.models.structured.SalesEmail;
import lombok.Getter;

import java.util.List;

// Engaging sales agent with structured email output.
@Getter
public class StructuredEngagingSalesAgent extends AIAgentComponent {
    private final List<GuardrailFunction> guardrails;

    public StructuredEngagingSalesAgent(String model, List<GuardrailFunction> guardrails) {
        super(model);
        this.guardrails = guardrails;
    }

    @Override
    protected String getAgentName() {
        return "Engaging Sales Agent (Structured)";
    }

    @Override
    protected String getPromptId() {
        return "sales/engaging-sales-agent";
    }

    @Override
    protected Class<?> getOutputType() {
        return SalesEmail.class;
    }

    @Override
    protected List<GuardrailFunction> getGuardrails() {
        return guardrails;
    }
}
