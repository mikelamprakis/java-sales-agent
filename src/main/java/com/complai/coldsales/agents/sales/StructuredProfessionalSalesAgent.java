package com.complai.coldsales.agents.sales;

import com.complai.coldsales.agents.base.core.AIAgentComponent;
import com.complai.coldsales.agents.base.guardrails.GuardrailFunction;
import com.complai.coldsales.models.structured.SalesEmail;
import lombok.Getter;

import java.util.List;

// Professional sales agent with structured email output.
@Getter
public class StructuredProfessionalSalesAgent extends AIAgentComponent {
    private final List<GuardrailFunction> guardrails;

    public StructuredProfessionalSalesAgent(String model, List<GuardrailFunction> guardrails) {
        super(model);
        this.guardrails = guardrails;
    }

    @Override
    protected String getAgentName() {
        return "Professional Sales Agent (Structured)";
    }

    @Override
    protected String getPromptId() {
        return "sales/professional-sales-agent";
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
