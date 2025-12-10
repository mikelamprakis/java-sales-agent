package com.complai.coldsales.models.types;

/**
 * Email tone options.
 * Enumeration of possible email tones for sales emails.
 */
public enum EmailTone {
    PROFESSIONAL("professional"),
    ENGAGING("engaging"),
    CASUAL("casual"),
    URGENT("urgent"),
    FRIENDLY("friendly");

    private final String value;

    EmailTone(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static EmailTone fromString(String value) {
        if (value == null || value.isBlank()) {
            return PROFESSIONAL; // default
        }
        for (EmailTone tone : EmailTone.values()) {
            if (tone.value.equalsIgnoreCase(value)) {
                return tone;
            }
        }
        return PROFESSIONAL; // default
    }
    
    @Override
    public String toString() {
        return "EmailTone{" +
                "value='" + value + '\'' +
                '}';
    }
}

