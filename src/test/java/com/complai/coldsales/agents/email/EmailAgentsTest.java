package com.complai.coldsales.agents.email;

import com.complai.coldsales.TestUtils;
import com.complai.coldsales.agents.base.core.Agent;
import com.complai.coldsales.config.Settings;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for email agents.
 */
class EmailAgentsTest {
    
    private Settings settings;
    
    @BeforeEach
    void setUp() {
        settings = TestUtils.createTestSettings();
    }
    
    @Test
    void testHTMLConverterAgent() {
        HTMLConverterAgent agentWrapper = new HTMLConverterAgent(settings.getModel());
        Agent agent = agentWrapper.getAgent();
        
        assertNotNull(agent);
        assertEquals("HTML Email Body Converter", agent.getName());
        assertFalse(agent.hasStructuredOutput()); // HTML converter returns plain text HTML
        assertEquals(settings.getModel(), agent.getModel());
    }
    
}

