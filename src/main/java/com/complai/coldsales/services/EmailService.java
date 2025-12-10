package com.complai.coldsales.services;

import com.complai.coldsales.config.Settings;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Email sending functionality supporting multiple providers.
 */
public class EmailService implements ServiceTool {
    
    private final Settings settings;
    private final String provider;

    public EmailService(Settings settings) {
        this.settings = settings;
        this.provider = settings.getEmailProvider().toLowerCase();
    }

    @Override
    public String getServiceName() {
        return "EmailService(" + provider + ")";
    }

    /**
     * Send a plain text email using the configured provider.
     */
    public Map<String, String> sendTextEmail(String body, String subject) {
        if ("smtp".equals(provider)) {
            return sendViaSmtp(body, subject, false);
        } else {
            Map<String, String> result = new HashMap<>();
            result.put("status", "error");
            result.put("message", "Unsupported email provider: " + provider);
            return result;
        }
    }

    /**
     * Send an HTML email using the configured provider.
     */
    public Map<String, String> sendHtmlEmail(String htmlBody, String subject) {
        if ("smtp".equals(provider)) {
            return sendViaSmtp(htmlBody, subject, true);
        } else {
            Map<String, String> result = new HashMap<>();
            result.put("status", "error");
            result.put("message", "Unsupported email provider: " + provider);
            return result;
        }
    }

    /**
     * Send a test email to verify configuration.
     */
    public Map<String, String> sendTestEmail() {
        return sendTextEmail(
            "This is a test email from the Cold Sales Agent System.",
            "Test Email - Cold Sales Agent"
        );
    }

    /**
     * Send email via SMTP (Gmail, etc.).
     */
    private Map<String, String> sendViaSmtp(String body, String subject, boolean isHtml) {
        Map<String, String> result = new HashMap<>();
        
        try {
            // Setup mail server properties
            Properties props = new Properties();
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.host", settings.getSmtpServer());
            props.put("mail.smtp.port", String.valueOf(settings.getSmtpPort()));

            // Create session
            Session session = Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(
                        settings.getSmtpUsername(),
                        settings.getSmtpPassword()
                    );
                }
            });

            // Create message
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(settings.getFromEmail()));
            message.setRecipients(Message.RecipientType.TO,
                InternetAddress.parse(settings.getToEmail()));
            message.setSubject(subject);

            // Set content
            if (isHtml) {
                message.setContent(body, "text/html; charset=utf-8");
            } else {
                message.setText(body);
            }

            // Send message
            Transport.send(message);

            result.put("status", "success");
            result.put("message", (isHtml ? "HTML " : "") + 
                                 "Email sent successfully via SMTP");
            
        } catch (Exception e) {
            result.put("status", "error");
            result.put("message", "SMTP error: " + e.getMessage());
            e.printStackTrace();
        }
        
        return result;
    }
}
