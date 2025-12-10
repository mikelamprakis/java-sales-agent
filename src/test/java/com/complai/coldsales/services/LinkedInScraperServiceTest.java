package com.complai.coldsales.services;

import com.complai.coldsales.utils.Result;
import com.complai.coldsales.utils.ServiceError;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for LinkedInScraper.
 */
class LinkedInScraperServiceTest {

    private final LinkedInScraperService linkedInScraperService = new LinkedInScraperService();
    
    @Test
    void testFindLinkedInUrl() throws ExecutionException, InterruptedException {
        CompletableFuture<Result<String, ServiceError>> future = linkedInScraperService.findLinkedInUrl("Google");
        Result<String, ServiceError> result = future.get();
        
        assertTrue(result.isOk());
        String url = result.unwrap();
        assertNotNull(url);
        assertTrue(url.contains("linkedin.com/company/"));
        assertTrue(url.contains("google"));
    }
    
    @Test
    void testFindLinkedInUrlWithSpecialCharacters() throws ExecutionException, InterruptedException {
        CompletableFuture<Result<String, ServiceError>> future = linkedInScraperService.findLinkedInUrl("AT&T Inc.");
        Result<String, ServiceError> result = future.get();
        
        assertTrue(result.isOk());
        String url = result.unwrap();
        assertNotNull(url);
        assertTrue(url.contains("linkedin.com/company/"));
    }
    
    @Test
    void testScrapeCompanyPage() throws ExecutionException, InterruptedException {
        // This will likely fail due to LinkedIn's anti-scraping, but we test the structure
        CompletableFuture<Result<LinkedInScraperService.LinkedInCompanyData, ServiceError>> future =
                linkedInScraperService.scrapeCompanyPage("Google");
        Result<LinkedInScraperService.LinkedInCompanyData, ServiceError> result = future.get();
        
        // Result may be Ok or Err depending on LinkedIn's response
        if (result.isOk()) {
            LinkedInScraperService.LinkedInCompanyData data = result.unwrap();
            assertNotNull(data);
            assertEquals("Google", data.getCompanyName());
            assertNotNull(data.getLinkedInUrl());
        } else {
            // Expected - LinkedIn blocks scrapers
            ServiceError error = result.unwrapErr();
            assertNotNull(error);
            assertTrue(error.message().contains("LinkedIn"));
        }
    }
    
    @Test
    void testLinkedInCompanyDataToAnalysisPrompt() {
        LinkedInScraperService.LinkedInCompanyData data = new LinkedInScraperService.LinkedInCompanyData(
                "Test Company",
                "https://linkedin.com/company/test",
                "Company description",
                "1000-5000 employees",
                "Technology",
                "San Francisco, CA"
        );
        
        String prompt = data.toAnalysisPrompt();
        
        assertNotNull(prompt);
        assertTrue(prompt.contains("Test Company"));
        assertTrue(prompt.contains("LINKEDIN COMPANY"));
    }
}

