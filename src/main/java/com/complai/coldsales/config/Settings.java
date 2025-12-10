package com.complai.coldsales.config;

import io.github.cdimascio.dotenv.Dotenv;
import lombok.Builder;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

/**
 * Application settings loaded from environment variables.
 */
@Data
@Builder
public class Settings {

    private static final Logger log = LoggerFactory.getLogger(Settings.class);
    
    // Required Configuration
    private String openaiApiKey;
    private String fromEmail;
    private String toEmail;
    
    // Optional Configuration with defaults
    private String model;
    private String emailProvider;
    
    // Multiple AI Model Support
    private String googleApiKey;
    private String deepseekApiKey;
    private String groqApiKey;
    
    // SMTP Configuration
    private String smtpServer;
    private int smtpPort;
    private String smtpUsername;
    private String smtpPassword;
    
    // Optional SSL Configuration
    private String sslCertFile;

    /**
     * Load settings from environment variables.
     */
    public static Settings loadSettings() {
        log.info("📝 Loading configuration...");
        Dotenv dotenv = Dotenv.configure()
                .ignoreIfMissing()
                .load();
        
        // Required settings
        String openaiApiKey = Optional.ofNullable(dotenv.get("OPENAI_API_KEY")).orElseThrow(() -> new IllegalStateException("OPENAI_API_KEY environment variable is required"));
        
        String fromEmail = dotenv.get("FROM_EMAIL");
        if (fromEmail == null || fromEmail.isEmpty()) {
            throw new IllegalStateException("FROM_EMAIL environment variable is required");
        }
        
        String toEmail = dotenv.get("TO_EMAIL");
        if (toEmail == null || toEmail.isEmpty()) {
            throw new IllegalStateException("TO_EMAIL environment variable is required");
        }
        
        // Email provider (fixed to SMTP)
        String emailProvider = "smtp";
        
        // SMTP validation
        String smtpPassword = dotenv.get("SMTP_PASSWORD");
        if (smtpPassword == null || smtpPassword.isEmpty()) {
            log.warn("⚠️  SMTP_PASSWORD not set. Gmail requires an app password.");
        }
        
        // Optional settings
        String model = dotenv.get("MODEL", "gpt-4o-mini");
        String sslCertFile = dotenv.get("SSL_CERT_FILE");
        
        // Optional AI model API keys
        String googleApiKey = dotenv.get("GOOGLE_API_KEY");
        String deepseekApiKey = dotenv.get("DEEPSEEK_API_KEY");
        String groqApiKey = dotenv.get("GROQ_API_KEY");
        
        String smtpServer = dotenv.get("SMTP_SERVER", "smtp.gmail.com");
        int smtpPort = Integer.parseInt(dotenv.get("SMTP_PORT", "587"));
        String smtpUsername = dotenv.get("SMTP_USERNAME", fromEmail);
        
        return Settings.builder()
                .openaiApiKey(openaiApiKey)
                .model(model)
                .emailProvider(emailProvider)
                .googleApiKey(googleApiKey)
                .deepseekApiKey(deepseekApiKey)
                .groqApiKey(groqApiKey)
                .smtpServer(smtpServer)
                .smtpPort(smtpPort)
                .smtpUsername(smtpUsername)
                .smtpPassword(smtpPassword != null ? smtpPassword : "")
                .fromEmail(fromEmail)
                .toEmail(toEmail)
                .sslCertFile(sslCertFile)
                .build();
    }
}
