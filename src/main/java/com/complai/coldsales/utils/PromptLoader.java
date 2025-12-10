package com.complai.coldsales.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * Utility class for loading prompts from resource files.
 * All prompts are stored under src/main/resources/prompts/ (organized into subfolders).
 */
public class PromptLoader {
    
    private static final String PROMPTS_BASE_PATH = "prompts/";
    
    /**
     * Load a prompt from the resources/prompts directory.
     * 
     * @param promptName The name of the prompt file (without path)
     * @return The prompt content as a string
     * @throws IllegalStateException if the prompt file cannot be loaded
     */
    public static String loadPrompt(String promptName) {
        String resourcePath = PROMPTS_BASE_PATH + promptName;
        
        try (InputStream inputStream = PromptLoader.class.getClassLoader().getResourceAsStream(resourcePath)) {
            if (inputStream == null) {
                throw new IllegalStateException("Prompt file not found: " + resourcePath + 
                    ". Make sure the file exists under src/main/resources/prompts/ with the correct subfolder path.");
            }
            
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
                StringBuilder contentBuilder = new StringBuilder();
                String line;
                try {
                    while ((line = reader.readLine()) != null) {
                        if (!contentBuilder.isEmpty()) {
                            contentBuilder.append("\n");
                        }
                        contentBuilder.append(line);
                    }
                } catch (IOException e) {
                    throw new IllegalStateException("Failed to read prompt file: " + resourcePath, e);
                }
                String content = contentBuilder.toString();
                
                if (content.trim().isEmpty()) {
                    throw new IllegalStateException("Prompt file is empty: " + resourcePath);
                }
                return content;
            }
        } catch (Exception e) {
            throw new IllegalStateException("Failed to load prompt: " + resourcePath, e);
        }
    }
    
    /**
     * Load a prompt with a fallback to a default value if file is not found.
     * 
     * @param promptName The name of the prompt file
     * @param fallback The fallback content if file is not found
     * @return The prompt content or fallback
     */
    public static String loadPromptWithFallback(String promptName, String fallback) {
        try {
            return loadPrompt(promptName);
        } catch (IllegalStateException e) {
            System.err.println("⚠️  Warning: Could not load prompt '" + promptName + "', using fallback");
            return fallback;
        }
    }
}

