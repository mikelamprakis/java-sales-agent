package com.complai.coldsales.agents.email;

import com.complai.coldsales.agents.base.core.AIAgentComponent;
import lombok.Getter;

/**
 * Agent that converts text emails to HTML format.
 */
@Getter
public class HTMLConverterAgent extends AIAgentComponent {
    
    public HTMLConverterAgent(String model) {
        super(model);
    }

    @Override
    protected String getAgentName() {
        return "HTML Email Body Converter";
    }

    @Override
    protected String getPromptId() {
        return "email/html-converter-agent";
    }
}
