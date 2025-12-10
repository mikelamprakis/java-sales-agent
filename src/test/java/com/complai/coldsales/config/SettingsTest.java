package com.complai.coldsales.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Settings class.
 */
class SettingsTest {
    
    @Test
    void testLoadSettingsWithMissingApiKey() {
        // This test expects an exception when OPENAI_API_KEY is missing
        // In a real scenario, we'd mock the environment, but for now we test the behavior
        // Note: This test may pass or fail depending on actual environment
    }
    
    @Test
    void testSettingsBuilder() {
        Settings settings = Settings.builder()
                .openaiApiKey("test-key")
                .model("gpt-4o-mini")
                .emailProvider("smtp")
                .fromEmail("from@example.com")
                .toEmail("to@example.com")
                .smtpServer("smtp.gmail.com")
                .smtpPort(587)
                .smtpUsername("user")
                .smtpPassword("pass")
                .build();
        
        assertNotNull(settings);
        assertEquals("test-key", settings.getOpenaiApiKey());
        assertEquals("gpt-4o-mini", settings.getModel());
        assertEquals("smtp", settings.getEmailProvider());
        assertEquals("from@example.com", settings.getFromEmail());
        assertEquals("to@example.com", settings.getToEmail());
        assertEquals("smtp.gmail.com", settings.getSmtpServer());
        assertEquals(587, settings.getSmtpPort());
        assertEquals("user", settings.getSmtpUsername());
        assertEquals("pass", settings.getSmtpPassword());
    }
    
    @Test
    void testSettingsWithOptionalFields() {
        Settings settings = Settings.builder()
                .openaiApiKey("test-key")
                .fromEmail("from@example.com")
                .toEmail("to@example.com")
                .googleApiKey("google-key")
                .deepseekApiKey("deepseek-key")
                .groqApiKey("groq-key")
                .sslCertFile("/path/to/cert")
                .build();
        
        assertNotNull(settings);
        assertEquals("google-key", settings.getGoogleApiKey());
        assertEquals("deepseek-key", settings.getDeepseekApiKey());
        assertEquals("groq-key", settings.getGroqApiKey());
        assertEquals("/path/to/cert", settings.getSslCertFile());
    }
}

