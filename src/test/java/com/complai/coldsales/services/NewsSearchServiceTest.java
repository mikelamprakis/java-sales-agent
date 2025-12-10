package com.complai.coldsales.services;

import com.complai.coldsales.utils.Result;
import com.complai.coldsales.utils.ServiceError;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for NewsSearch.
 */
class NewsSearchServiceTest {

    private final NewsSearchService newsSearchService = new NewsSearchService();
    
    @Test
    void testSearchRecentNews() throws ExecutionException, InterruptedException {
        CompletableFuture<Result<List<NewsSearchService.NewsArticle>, ServiceError>> future = 
                newsSearchService.searchRecentNews("Apple");
        Result<List<NewsSearchService.NewsArticle>, ServiceError> result = future.get();
        
        // Result may be Ok or Err depending on scraping success
        if (result.isOk()) {
            List<NewsSearchService.NewsArticle> articles = result.unwrap();
            assertNotNull(articles);
            // May be empty if no articles found, but structure should be valid
        } else {
            // Expected - Google may block scrapers
            ServiceError error = result.unwrapErr();
            assertNotNull(error);
            assertTrue(error.message().contains("news"));
        }
    }
    
    @Test
    void testNewsArticleCreation() {
        NewsSearchService.NewsArticle article = new NewsSearchService.NewsArticle(
                "Test Title",
                "https://example.com/article",
                "Test snippet",
                "Test Source",
                "2 days ago"
        );
        
        assertEquals("Test Title", article.getTitle());
        assertEquals("https://example.com/article", article.getUrl());
        assertEquals("Test snippet", article.getSnippet());
        assertEquals("Test Source", article.getSource());
        assertEquals("2 days ago", article.getDate());
    }
    
    @Test
    void testNewsArticleToSummary() {
        NewsSearchService.NewsArticle article = new NewsSearchService.NewsArticle(
                "Test Title",
                "https://example.com/article",
                "Test snippet",
                "Test Source",
                "2 days ago"
        );
        
        String summary = article.toSummary();
        
        assertNotNull(summary);
        assertTrue(summary.contains("Test Title"));
        assertTrue(summary.contains("https://example.com/article"));
        assertTrue(summary.contains("Test Source"));
    }
    
    @Test
    void testFormatArticlesForAnalysis() {
        List<NewsSearchService.NewsArticle> articles = List.of(
                new NewsSearchService.NewsArticle("Title 1", "url1", "snippet1", "source1", "1 day ago"),
                new NewsSearchService.NewsArticle("Title 2", "url2", "snippet2", "source2", "2 days ago")
        );
        
        String formatted = newsSearchService.formatArticlesForAnalysis(articles);
        
        assertNotNull(formatted);
        assertTrue(formatted.contains("Title 1"));
        assertTrue(formatted.contains("Title 2"));
        assertTrue(formatted.contains("2 found"));
    }
    
    @Test
    void testFormatArticlesForAnalysisEmpty() {
        List<NewsSearchService.NewsArticle> articles = List.of();
        String formatted = newsSearchService.formatArticlesForAnalysis(articles);
        
        assertNotNull(formatted);
        assertTrue(formatted.contains("No recent news found"));
    }
}

