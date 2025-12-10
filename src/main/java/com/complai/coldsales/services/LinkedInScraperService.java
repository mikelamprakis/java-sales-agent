package com.complai.coldsales.services;

import com.complai.coldsales.utils.Result;
import com.complai.coldsales.utils.ServiceError;
import lombok.AllArgsConstructor;
import lombok.Getter;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Service that attempts to fetch LinkedIn company data (demo purposes).
 */
public class LinkedInScraperService implements ServiceTool {

    private static final int TIMEOUT_MS = 10000;
    private static final String USER_AGENT = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36";
    private final Executor executor;
    private final OkHttpClient httpClient;

    public LinkedInScraperService() {
        this(ForkJoinPool.commonPool(), new OkHttpClient());
    }

    public LinkedInScraperService(Executor executor, OkHttpClient httpClient) {
        this.executor = executor != null ? executor : ForkJoinPool.commonPool();
        this.httpClient = httpClient != null ? httpClient : new OkHttpClient();
    }

    public CompletableFuture<Result<String, ServiceError>> findLinkedInUrl(String companyName) {
        return CompletableFuture.supplyAsync(() -> {
            String slug = companyName.toLowerCase()
                    .replaceAll("[^a-z0-9]+", "-")
                    .replaceAll("^-|-$", "");

            String url = "https://www.linkedin.com/company/" + slug;
            System.out.println("🔍 Attempting LinkedIn URL: " + url);
            System.out.println("⚠️  Note: LinkedIn scraping may be blocked. Consider using LinkedIn API or third-party services.");
            return Result.ok(url);
        }, executor);
    }

    public CompletableFuture<Result<LinkedInCompanyData, ServiceError>> scrapeCompanyPage(String companyName) {
        return findLinkedInUrl(companyName)
                .thenCompose(urlResult -> {
                    if (urlResult.isErr()) {
                        ServiceError urlError = urlResult.unwrapErr();
                        return CompletableFuture.completedFuture(
                                Result.err(new ServiceError(
                                        "LinkedInScraperService",
                                        "scrapeCompanyPage",
                                        "Failed to construct LinkedIn URL for: " + companyName + ". " + urlError.getUserMessage()
                                ))
                        );
                    }
                    
                    String url = urlResult.unwrap();
                    return CompletableFuture.supplyAsync(() -> {
                        try {
                            System.out.println("🔗 Fetching LinkedIn page: " + url);
                            Document doc = fetchDocument(url);

                            return Result.ok(extractCompanyData(doc, companyName, url));

                        } catch (IOException e) {
                            System.err.println("⚠️  LinkedIn scraping failed (expected - LinkedIn blocks scrapers)");
                            System.err.println("   Error: " + e.getMessage());
                            System.err.println("   💡 Recommendation: Use LinkedIn Official API or services like Apify/ScraperAPI");
                            return Result.err(new ServiceError(
                                    "LinkedInScraperService",
                                    "scrapeCompanyPage",
                                    "LinkedIn scraping failed for: " + companyName + ". LinkedIn blocks scrapers. Use LinkedIn Official API or services like Apify/ScraperAPI.",
                                    e
                            ));
                        }
                    }, executor);
                });
    }

    private Document fetchDocument(String url) throws IOException {
        Request request = new Request.Builder()
                .url(url)
                .header("User-Agent", USER_AGENT)
                .build();
        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected HTTP status " + response.code());
            }
            ResponseBody body = response.body();
            if (body == null) {
                throw new IOException("Empty response body");
            }
            String html = body.string();
            return Jsoup.parse(html, url);
        }
    }

    private LinkedInCompanyData extractCompanyData(Document doc, String companyName, String url) {
        String description = doc.select("meta[property=og:description]").attr("content");
        if (description.isEmpty()) {
            description = doc.select("meta[name=description]").attr("content");
        }

        String employeeCount = "";
        String pageText = doc.text();
        if (pageText.contains("employees") || pageText.contains("followers")) {
            String[] parts = pageText.split("\\s+");
            for (int i = 0; i < parts.length - 1; i++) {
                if (parts[i].matches("\\d+") &&
                        (parts[i + 1].toLowerCase().contains("employee") ||
                                parts[i + 1].toLowerCase().contains("follower"))) {
                    employeeCount = parts[i] + " " + parts[i + 1];
                    break;
                }
            }
        }

        return new LinkedInCompanyData(
                companyName,
                url,
                description,
                employeeCount,
                "",
                ""
        );
    }

    @Override
    public String getServiceName() {
        return "LinkedInScraperService";
    }

    @Getter
    @AllArgsConstructor
    public static class LinkedInCompanyData {
        private final String companyName;
        private final String linkedInUrl;
        private final String description;
        private final String employeeCount;
        private final String industry;
        private final String location;

        public String toAnalysisPrompt() {
            return Stream.of(
                    "LINKEDIN COMPANY: " + companyName,
                    "URL: " + linkedInUrl,
                    "",
                    Stream.of(
                            description.isEmpty() ? null : "DESCRIPTION: " + description,
                            (employeeCount.isEmpty() || "Unknown".equals(employeeCount)) ? null : "EMPLOYEE COUNT: " + employeeCount,
                            (industry.isEmpty() || "Unknown".equals(industry)) ? null : "INDUSTRY: " + industry,
                            (location.isEmpty() || "Unknown".equals(location)) ? null : "LOCATION: " + location
                    ).filter(Objects::nonNull)
                     .collect(Collectors.joining("\n\n"))
            ).filter(Objects::nonNull)
             .collect(Collectors.joining("\n\n"));
        }
    }
}

