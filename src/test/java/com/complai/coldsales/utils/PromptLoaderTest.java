package com.complai.coldsales.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for PromptLoader.
 */
class PromptLoaderTest {
    
    @Test
    void testLoadPrompt() {
        // Test loading an existing prompt
        String prompt = PromptLoader.loadPrompt("sales/professional-sales-agent");
        
        assertNotNull(prompt);
        assertFalse(prompt.trim().isEmpty());
    }
    
    @Test
    void testLoadNonExistentPrompt() {
        assertThrows(IllegalStateException.class, () -> {
            PromptLoader.loadPrompt("non-existent-prompt");
        });
    }
    
    @Test
    void testLoadPromptWithFallback() {
        String fallback = "This is a fallback prompt";
        String result = PromptLoader.loadPromptWithFallback("non-existent-prompt", fallback);
        
        assertEquals(fallback, result);
    }
    
    @Test
    void testLoadPromptWithFallbackWhenExists() {
        String fallback = "This is a fallback prompt";
        String result = PromptLoader.loadPromptWithFallback("sales/professional-sales-agent", fallback);
        
        assertNotNull(result);
        assertNotEquals(fallback, result); // Should load actual prompt, not fallback
    }
}

