package com.complai.coldsales.models.pipeline.email;

/**
 * Represents the selected email from the pipeline.
 * Part of the email pipeline results.
 */
public class SelectedEmail {
    private final String subject;
    private final String body;
    private final String tone;
    private final int expectedResponseRate;
    
    public SelectedEmail(String subject, String body, String tone, int expectedResponseRate) {
        this.subject = subject;
        this.body = body;
        this.tone = tone;
        this.expectedResponseRate = expectedResponseRate;
    }
    
    public String getSubject() {
        return subject;
    }
    
    public String getBody() {
        return body;
    }
    
    public String getTone() {
        return tone;
    }
    
    public int getExpectedResponseRate() {
        return expectedResponseRate;
    }
    
    @Override
    public String toString() {
        return "SelectedEmail{" +
                "subject='" + subject + '\'' +
                ", body='" + (body != null ? body.substring(0, Math.min(50, body.length())) + "..." : "null") + '\'' +
                ", tone='" + tone + '\'' +
                ", expectedResponseRate=" + expectedResponseRate +
                '}';
    }
}

