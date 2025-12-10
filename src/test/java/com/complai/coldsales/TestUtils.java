package com.complai.coldsales;

import com.complai.coldsales.config.Settings;
import io.github.cdimascio.dotenv.Dotenv;

import java.util.Optional;

/**
 * Test utilities for creating test fixtures and mocks.
 */
public class TestUtils {
    
    /**
     * Create a test Settings object with test values.
     * Uses environment variables if available, otherwise uses safe defaults.
     */
    public static Settings createTestSettings() {
        Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
        
        // Try to use real values from env, but provide safe defaults for unit tests
        String openaiApiKey = Optional.ofNullable(dotenv.get("OPENAI_API_KEY"))
                .orElse("test-api-key");
        String fromEmail = Optional.ofNullable(dotenv.get("FROM_EMAIL"))
                .orElse("test@example.com");
        String toEmail = Optional.ofNullable(dotenv.get("TO_EMAIL"))
                .orElse("recipient@example.com");
        String model = Optional.ofNullable(dotenv.get("MODEL"))
                .orElse("gpt-4o-mini");
        String smtpPassword = Optional.ofNullable(dotenv.get("SMTP_PASSWORD"))
                .orElse("test-password");
        
        return Settings.builder()
                .openaiApiKey(openaiApiKey)
                .model(model)
                .emailProvider("smtp")
                .smtpServer("smtp.gmail.com")
                .smtpPort(587)
                .smtpUsername(fromEmail)
                .smtpPassword(smtpPassword)
                .fromEmail(fromEmail)
                .toEmail(toEmail)
                .build();
    }
    
    /**
     * Check if we should run real API tests (requires OPENAI_API_KEY).
     */
    public static boolean shouldRunRealApiTests() {
        Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
        String apiKey = dotenv.get("OPENAI_API_KEY");
        return apiKey != null && !apiKey.isEmpty() && !apiKey.equals("test-api-key");
    }
    
    /**
     * Create a minimal test prompt.
     */
    public static String createTestPrompt() {
        return "Write a cold sales email for ComplAI, our SOC2 compliance automation platform.\n\n" +
               "Target: CTO at a 200-person SaaS company\n" +
               "Pain point: Manual audit preparation taking weeks of engineering time\n" +
               "Value prop: Reduce audit prep from weeks to days with AI automation\n" +
               "Goal: Schedule a 15-minute demo call";
    }
}

