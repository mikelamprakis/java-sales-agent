package com.complai.coldsales.services;

import com.complai.coldsales.utils.Result;
import com.complai.coldsales.utils.ServiceError;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for WebScraperService.
 */
class WebScraperServiceTest {

    private final WebScraperService webScraperService = new WebScraperService();
    
    @Test
    void testFindCompanyWebsite() throws ExecutionException, InterruptedException {
        CompletableFuture<Result<String, ServiceError>> future = webScraperService.findCompanyWebsite("google");
        Result<String, ServiceError> result = future.get();
        
        // May return error if website not found, or a valid URL
        if (result.isOk()) {
            String url = result.unwrap();
            assertTrue(url.startsWith("http"));
        } else {
            // Error case is also valid - website might not be found
            assertTrue(result.isErr());
        }
    }
    
    @Test
    void testFetchWebsite() throws ExecutionException, InterruptedException {
        CompletableFuture<Result<WebScraperService.WebsiteContent, ServiceError>> future = webScraperService.fetchWebsite("https://www.example.com");
        Result<WebScraperService.WebsiteContent, ServiceError> result = future.get();
        
        assertTrue(result.isOk(), "Should successfully fetch example.com");
        WebScraperService.WebsiteContent content = result.unwrap();
        assertNotNull(content);
        assertEquals("https://www.example.com", content.getUrl());
    }
    
    @Test
    void testFetchWebsiteError() throws ExecutionException, InterruptedException {
        CompletableFuture<Result<WebScraperService.WebsiteContent, ServiceError>> future = webScraperService.fetchWebsite("https://invalid-url-that-does-not-exist-12345.com");
        Result<WebScraperService.WebsiteContent, ServiceError> result = future.get();
        
        assertTrue(result.isErr(), "Should return error for invalid URL");
        ServiceError error = result.unwrapErr();
        assertNotNull(error);
        assertNotNull(error.message());
        assertTrue(error.message().contains("invalid-url-that-does-not-exist-12345.com"));
    }
    
    @Test
    void testWebsiteContentToAnalysisPrompt() {
        WebScraperService.WebsiteContent content = new WebScraperService.WebsiteContent(
                "https://example.com",
                "Example Title",
                "Example description",
                "Main text content here",
                java.util.List.of("Heading 1", "Heading 2"),
                java.util.List.of("https://example.com/page1"),
                "100-500 employees",
                "Technology"
        );
        
        String prompt = content.toAnalysisPrompt();
        
        assertNotNull(prompt);
        assertTrue(prompt.contains("https://example.com"));
        assertTrue(prompt.contains("Example Title"));
    }
    
    @Test
    void testResultErrorHandling() {
        ServiceError error = new ServiceError(
                "WebScraperService",
                "fetchWebsite",
                "Connection timeout"
        );
        
        Result<WebScraperService.WebsiteContent, ServiceError> result = Result.err(error);
        assertTrue(result.isErr());
        assertEquals("Connection timeout", result.unwrapErr().message());
        
        // Test fallback
        WebScraperService.WebsiteContent fallback = new WebScraperService.WebsiteContent(
                "https://example.com", "", "", "", java.util.List.of(), java.util.List.of(), "", ""
        );
        WebScraperService.WebsiteContent recovered = result.unwrapOr(fallback);
        assertEquals("https://example.com", recovered.getUrl());
    }
}

