package com.complai.coldsales.utils;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.regex.Pattern;

/**
 * Declarative builder for constructing augmented prompts in service-backed tools.
 * 
 * Eliminates duplication across CompanyWebsiteTool, LinkedInCompanyTool, and NewsAndPressTool
 * by centralizing company name extraction and prompt formatting logic.
 */
public final class ToolPromptBuilder {
    
    private static final Pattern COMPANY_NAME_PATTERN = Pattern.compile(
            "Research\\s+([A-Za-z0-9\\s&.-]+?)\\s+(?:for|to|at)", Pattern.CASE_INSENSITIVE
    );
    
    private final String originalPrompt;
    private String companyName;
    
    private ToolPromptBuilder(String originalPrompt) {
        this.originalPrompt = originalPrompt;
    }
    
    /**
     * Start building a prompt from the original user input.
     */
    public static ToolPromptBuilder from(String originalPrompt) {
        return new ToolPromptBuilder(originalPrompt);
    }
    
    /**
     * Extract company name from the prompt using multiple strategies.
     * Falls back to the original prompt if extraction fails.
     */
    public ToolPromptBuilder withExtractedCompanyName() {
        this.companyName = extractCompanyName(originalPrompt);
        return this;
    }
    
    /**
     * Use a specific company name (useful when name is known from context).
     */
    public ToolPromptBuilder withCompanyName(String companyName) {
        this.companyName = companyName;
        return this;
    }
    
    /**
     * Build a prompt that wraps service data with a standard analysis instruction.
     * 
     * @param dataFormatter Function that converts service data to a formatted string
     * @param analysisType Type of analysis (e.g., "company website data", "LinkedIn company data")
     * @return CompletableFuture with the augmented prompt
     */
    public <T> CompletableFuture<String> wrapServiceData(
            CompletableFuture<T> serviceDataFuture,
            Function<T, String> dataFormatter,
            String analysisType
    ) {
        return serviceDataFuture
                .thenApply(data -> buildAnalysisPrompt(dataFormatter.apply(data), analysisType));
    }
    
    /**
     * Build a prompt that wraps service data with a custom instruction.
     * 
     * @param dataFormatter Function that converts service data to a formatted string
     * @param instructionTemplate Template with {companyName} and {data} placeholders
     * @return CompletableFuture with the augmented prompt
     */
    public <T> CompletableFuture<String> wrapServiceDataWithCustomInstruction(
            CompletableFuture<T> serviceDataFuture,
            Function<T, String> dataFormatter,
            String instructionTemplate
    ) {
        return serviceDataFuture
                .thenApply(data -> {
                    String formattedData = dataFormatter.apply(data);
                    return instructionTemplate
                            .replace("{companyName}", getCompanyName())
                            .replace("{data}", formattedData);
                });
    }
    
    /**
     * Build a prompt for when service data is unavailable.
     */
    public CompletableFuture<String> buildFallbackPrompt(String fallbackMessage) {
        return CompletableFuture.completedFuture(
                String.format("Could not retrieve data for: %s. %s", getCompanyName(), fallbackMessage)
        );
    }
    
    /**
     * Wrap service data that returns a Result type, handling errors gracefully.
     * 
     * @param serviceDataFuture Future that returns Result<T, ServiceError>
     * @param dataFormatter Function that converts T to formatted string
     * @param analysisType Type of analysis
     * @return CompletableFuture with the augmented prompt or fallback message
     */
    public <T> CompletableFuture<String> wrapServiceDataResult(
            CompletableFuture<Result<T, ServiceError>> serviceDataFuture,
            Function<T, String> dataFormatter,
            String analysisType
    ) {
        return serviceDataFuture
                .thenApply(result -> {
                    if (result.isErr()) {
                        ServiceError error = result.unwrapErr();
                        System.err.println(error.format());
                        return buildAnalysisPrompt(
                                "Data unavailable: " + error.getUserMessage(),
                                analysisType
                        );
                    }
                    T data = result.unwrap();
                    return buildAnalysisPrompt(dataFormatter.apply(data), analysisType);
                });
    }
    
    /**
     * Wrap service data that returns a Result type with custom instruction.
     * 
     * @param serviceDataFuture Future that returns Result<T, ServiceError>
     * @param dataFormatter Function that converts T to formatted string
     * @param instructionTemplate Template with {companyName} and {data} placeholders
     * @return CompletableFuture with the augmented prompt or fallback message
     */
    public <T> CompletableFuture<String> wrapServiceDataResultWithCustomInstruction(
            CompletableFuture<Result<T, ServiceError>> serviceDataFuture,
            Function<T, String> dataFormatter,
            String instructionTemplate
    ) {
        return serviceDataFuture
                .thenApply(result -> {
                    if (result.isErr()) {
                        ServiceError error = result.unwrapErr();
                        System.err.println(error.format());
                        String fallbackData = "Data unavailable: " + error.getUserMessage();
                        return instructionTemplate
                                .replace("{companyName}", getCompanyName())
                                .replace("{data}", fallbackData);
                    }
                    T data = result.unwrap();
                    String formattedData = dataFormatter.apply(data);
                    return instructionTemplate
                            .replace("{companyName}", getCompanyName())
                            .replace("{data}", formattedData);
                });
    }
    
    /**
     * Get the extracted company name, or fallback to original prompt.
     */
    public String getCompanyName() {
        return companyName != null ? companyName : originalPrompt;
    }
    
    /**
     * Build a standard analysis prompt with the given data and analysis type.
     */
    private String buildAnalysisPrompt(String data, String analysisType) {
        return String.format("Analyze this %s:\n\n%s", analysisType, data);
    }
    
    /**
     * Extract company name using multiple strategies:
     * 1. Regex pattern matching "Research {Company} for/to/at"
     * 2. Simple word extraction (first few non-keywords)
     * 3. Fallback to original prompt
     */
    private static String extractCompanyName(String prompt) {
        if (prompt == null || prompt.isBlank()) {
            return prompt;
        }
        
        // Strategy 1: Regex pattern (most precise)
        var matcher = COMPANY_NAME_PATTERN.matcher(prompt);
        if (matcher.find()) {
            String extracted = matcher.group(1).trim();
            if (!extracted.isEmpty()) {
                return extracted;
            }
        }
        
        // Strategy 2: Simple word extraction (fallback)
        String[] parts = prompt.split("\\s+");
        if (parts.length > 0) {
            StringBuilder companyName = new StringBuilder();
            for (int i = 0; i < Math.min(parts.length, 3); i++) {
                String part = parts[i];
                if (isKeyword(part)) {
                    continue;
                }
                if (companyName.length() > 0) {
                    companyName.append(" ");
                }
                companyName.append(part);
            }
            String result = companyName.toString().trim();
            if (!result.isEmpty()) {
                return result;
            }
        }
        
        // Strategy 3: Fallback to original
        return prompt;
    }
    
    private static boolean isKeyword(String word) {
        String lower = word.toLowerCase();
        return lower.equals("research") || 
               lower.equals("for") || 
               lower.equals("to") || 
               lower.equals("at") ||
               lower.equals("about");
    }
}

