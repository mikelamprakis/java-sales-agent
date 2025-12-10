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
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Service that fetches real company website data.
 */
public class WebScraperService implements ServiceTool {

    private static final int TIMEOUT_MS = 10000;
    private static final String USER_AGENT = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36";
    private final Executor executor;
    private final OkHttpClient httpClient;

    public WebScraperService() {
        this(ForkJoinPool.commonPool(), new OkHttpClient());
    }

    public WebScraperService(Executor executor, OkHttpClient httpClient) {
        this.executor = executor != null ? executor : ForkJoinPool.commonPool();
        this.httpClient = httpClient != null ? httpClient : new OkHttpClient();
    }

    public CompletableFuture<Result<WebsiteContent, ServiceError>> fetchWebsite(String url) {
        return CompletableFuture.supplyAsync(() -> {
            System.out.println("🌐 Fetching website: " + url);
            try {
                Document doc = fetchDocument(url);
                return Result.ok(extractContent(doc, url));
            } catch (IOException e) {
                return Result.err(new ServiceError(
                        "WebScraperService",
                        "fetchWebsite",
                        "Failed to fetch website: " + url,
                        e
                ));
            }
        }, executor);
    }

    public CompletableFuture<Result<String, ServiceError>> findCompanyWebsite(String companyName) {
        return CompletableFuture.supplyAsync(() -> {
            String normalized = companyName.toLowerCase().replaceAll("\\s+", "");
            String[] patterns = {
                    "https://www." + normalized + ".com",
                    "https://" + normalized + ".com",
                    "https://www." + companyName.toLowerCase().replaceAll("\\s+", "-") + ".com"
            };

            for (String url : patterns) {
                try {
                    validateUrl(url);
                    System.out.println("✅ Found company website: " + url);
                    return Result.ok(url);
                } catch (Exception ignored) {
                    // Try next pattern
                }
            }

            System.out.println("⚠️  Could not auto-detect website for: " + companyName + "\n   Tip: In production, integrate with Google Custom Search API or SerpAPI");
            return Result.err(new ServiceError(
                    "WebScraperService",
                    "findCompanyWebsite",
                    "Could not auto-detect website for: " + companyName + ". Tip: Use Google Custom Search API or SerpAPI"
            ));
        }, executor);
    }

    private void validateUrl(String url) throws IOException {
        Request request = new Request.Builder()
                .url(url)
                .header("User-Agent", USER_AGENT)
                .build();
        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected HTTP status " + response.code());
            }
        }
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

    private WebsiteContent extractContent(Document doc, String url) {
        String title = doc.title();
        String description = doc.select("meta[name=description]").attr("content");
        if (description.isEmpty()) {
            description = doc.select("meta[property=og:description]").attr("content");
        }

        String mainText = extractMainText(doc);
        List<String> headings = doc.select("h1, h2, h3").stream()
                .map(Element::text)
                .filter(text -> !text.trim().isEmpty())
                .limit(20)
                .collect(Collectors.toList());

        List<String> importantLinks = doc.select("a[href]").stream()
                .map(link -> link.attr("abs:href"))
                .filter(href -> !href.isEmpty() && !href.startsWith("#"))
                .distinct()
                .limit(10)
                .collect(Collectors.toList());

        String companySize = extractCompanySize(doc);
        String industry = extractIndustry(doc);

        return new WebsiteContent(
                url,
                title,
                description,
                mainText,
                headings,
                importantLinks,
                companySize,
                industry
        );
    }

    private String extractMainText(Document doc) {
        Elements mainContent = doc.select("main, article, [role=main]");
        if (!mainContent.isEmpty()) {
            return mainContent.first().text();
        }

        Element body = doc.body();
        if (body != null) {
            body.select("nav, footer, header, script, style").remove();
            return body.text();
        }
        return "";
    }

    private String extractCompanySize(Document doc) {
        String[] sentences = doc.text().split("[.!?]");
        for (String sentence : sentences) {
            String lower = sentence.toLowerCase();
            if (lower.contains("employee") || lower.contains("team size")) {
                return sentence.trim();
            }
        }
        return "";
    }

    private String extractIndustry(Document doc) {
        String industry = doc.select("meta[property=og:type]").attr("content");
        if (!industry.isEmpty()) {
            return industry;
        }
        String text = doc.text().toLowerCase();
        String[] industries = {"technology", "software", "saas", "fintech", "healthcare",
                "manufacturing", "retail", "consulting", "finance"};
        for (String ind : industries) {
            if (text.contains(ind)) {
                return ind.substring(0, 1).toUpperCase() + ind.substring(1);
            }
        }
        return "";
    }

    @Override
    public String getServiceName() {
        return "WebScraperService";
    }

    @Getter
    @AllArgsConstructor
    public static class WebsiteContent {
        private final String url;
        private final String title;
        private final String description;
        private final String mainText;
        private final List<String> headings;
        private final List<String> importantLinks;
        private final String companySize;
        private final String industry;

        public String toAnalysisPrompt() {
            return Stream.of(
                    "WEBSITE: " + url,
                    "TITLE: " + title,
                    "",
                    Stream.of(
                            description.isEmpty() ? null : "DESCRIPTION: " + description,
                            companySize.isEmpty() ? null : "COMPANY SIZE: " + companySize,
                            industry.isEmpty() ? null : "INDUSTRY: " + industry
                    ).filter(Objects::nonNull).collect(Collectors.joining("\n\n")),
                    headings.isEmpty() ? null : "KEY SECTIONS:\n" + 
                            headings.stream()
                                    .map(h -> "- " + h)
                                    .collect(Collectors.joining("\n")),
                    "MAIN CONTENT (first 2000 chars):\n" + 
                            (mainText.length() > 2000 ? mainText.substring(0, 2000) + "..." : mainText)
            ).filter(Objects::nonNull)
             .collect(Collectors.joining("\n\n"));
        }

    }
}

