package com.complai.coldsales.agents.sales;

import com.complai.coldsales.agents.base.core.AIAgentComponent;
import com.complai.coldsales.agents.base.guardrails.GuardrailFunction;
import com.complai.coldsales.models.structured.SalesEmail;
import lombok.Getter;

import java.util.List;

// Busy executive sales agent with structured email output.
@Getter
public class StructuredBusySalesAgent extends AIAgentComponent {
    private final List<GuardrailFunction> guardrails;

    public StructuredBusySalesAgent(String model, List<GuardrailFunction> guardrails) {
        super(model);
        this.guardrails = guardrails;
    }

    @Override
    protected String getAgentName() {
        return "Busy Sales Agent (Structured)";
    }

    @Override
    protected String getPromptId() {
        return "sales/busy-sales-agent";
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
