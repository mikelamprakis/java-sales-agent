package com.complai.coldsales.tools;

import com.complai.coldsales.TestUtils;
import com.complai.coldsales.config.Settings;
import com.complai.coldsales.services.EmailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for EmailService.
 * 
 * Note: These tests don't actually send emails unless SMTP credentials are configured.
 */
class EmailToolsTest {
    
    private Settings settings;
    private EmailService emailService;
    
    @BeforeEach
    void setUp() {
        settings = TestUtils.createTestSettings();
        emailService = new EmailService(settings);
    }
    
    @Test
    void testEmailServiceCreation() {
        assertNotNull(emailService);
    }
    
    @Test
    void testSendTextEmail() {
        // This will attempt to send, but will likely fail without real SMTP credentials
        Map<String, String> result = emailService.sendTextEmail("Test body", "Test Subject");
        
        assertNotNull(result);
        assertTrue(result.containsKey("status"));
        // Status will be "error" if SMTP is not configured, or "success" if it is
    }
    
    @Test
    void testSendHtmlEmail() {
        String htmlBody = "<html><body><h1>Test</h1></body></html>";
        Map<String, String> result = emailService.sendHtmlEmail(htmlBody, "Test Subject");
        
        assertNotNull(result);
        assertTrue(result.containsKey("status"));
    }
    
    @Test
    void testSendTestEmail() {
        Map<String, String> result = emailService.sendTestEmail();
        
        assertNotNull(result);
        assertTrue(result.containsKey("status"));
    }
    
    @Test
    void testEmailToolsWithInvalidProvider() {
        Settings invalidSettings = Settings.builder()
                .openaiApiKey("test")
                .fromEmail("from@example.com")
                .toEmail("to@example.com")
                .emailProvider("invalid")
                .build();
        
        EmailService tools = new EmailService(invalidSettings);
        Map<String, String> result = tools.sendTextEmail("Test", "Test");
        
        assertEquals("error", result.get("status"));
        assertTrue(result.get("message").contains("Unsupported email provider"));
    }
}

