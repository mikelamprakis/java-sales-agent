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
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Service that scrapes recent news articles (demo implementation).
 */
public class NewsSearchService implements ServiceTool {

    private static final int TIMEOUT_MS = 10000;
    private static final String USER_AGENT = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36";
    private final Executor executor;
    private final OkHttpClient httpClient;

    public NewsSearchService() {
        this(ForkJoinPool.commonPool(), new OkHttpClient());
    }

    public NewsSearchService(Executor executor, OkHttpClient httpClient) {
        this.executor = executor != null ? executor : ForkJoinPool.commonPool();
        this.httpClient = httpClient != null ? httpClient : new OkHttpClient();
    }

    public CompletableFuture<Result<List<NewsArticle>, ServiceError>> searchRecentNews(String companyName) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String query = URLEncoder.encode(companyName + " news", StandardCharsets.UTF_8);
                String searchUrl = "https://www.google.com/search?q=" + query + "&tbm=nws&tbs=qdr:m";

                System.out.println("📰 Searching for recent news: " + companyName);
                System.out.println("   URL: " + searchUrl);

                Document doc = fetchDocument(searchUrl);

                List<NewsArticle> articles = extractNewsArticles(doc);
                return Result.ok(articles);

            } catch (IOException e) {
                System.err.println("⚠️  News search failed: " + e.getMessage());
                System.err.println("   💡 Recommendation: Use NewsAPI.org, SerpAPI, or Google News API for production");
                return Result.err(new ServiceError(
                        "NewsSearchService",
                        "searchRecentNews",
                        "Failed to search news for: " + companyName + ". Recommendation: Use NewsAPI.org, SerpAPI, or Google News API for production",
                        e
                ));
            }
        }, executor);
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

    private List<NewsArticle> extractNewsArticles(Document doc) {
        List<NewsArticle> articles = new ArrayList<>();
        Elements resultDivs = doc.select("div[data-ved]");

        for (Element result : resultDivs) {
            try {
                Element titleElement = result.selectFirst("h3");
                if (titleElement == null) continue;
                String title = titleElement.text();

                Element linkElement = result.selectFirst("a[href]");
                if (linkElement == null) continue;
                String url = linkElement.attr("abs:href");

                Element snippetElement = result.selectFirst("div[style*=-webkit-line-clamp]");
                if (snippetElement == null) {
                    snippetElement = result.selectFirst("span[style*=-webkit-line-clamp]");
                }
                String snippet = snippetElement != null ? snippetElement.text() : "";

                String source = "";
                Element sourceElement = result.selectFirst("span");
                if (sourceElement != null) {
                    source = sourceElement.text();
                }

                String date = "";
                Elements dateElements = result.select("span");
                for (Element dateEl : dateElements) {
                    String text = dateEl.text().toLowerCase();
                    if (text.contains("hour") || text.contains("day") || text.contains("week") ||
                            text.contains("month") || text.matches(".*\\d{1,2}\\s+(hour|day|week|month).*")) {
                        date = dateEl.text();
                        break;
                    }
                }

                if (!title.isEmpty() && !url.isEmpty()) {
                    articles.add(new NewsArticle(title, url, snippet, source, date));
                }

            } catch (Exception ignored) {
            }
        }

        return articles.stream().limit(5).collect(Collectors.toList());
    }

    public String formatArticlesForAnalysis(List<NewsArticle> articles) {
        if (articles.isEmpty()) {
            return "News search completed successfully, but no recent articles were found. " +
                   "This may indicate limited recent news coverage for this company. " +
                   "Please proceed with information from other research sources.";
        }

        return "RECENT NEWS ARTICLES (" + articles.size() + " found):\n\n" +
                IntStream.range(0, articles.size())
                        .mapToObj(i -> "Article " + (i + 1) + ":\n" + articles.get(i).toSummary())
                        .collect(Collectors.joining("\n"));
    }

    @Override
    public String getServiceName() {
        return "NewsSearchService";
    }

    @Getter
    @AllArgsConstructor
    public static class NewsArticle {
        private final String title;
        private final String url;
        private final String snippet;
        private final String source;
        private final String date;

        public String toSummary() {
            return Stream.of(
                    "TITLE: " + title,
                    source.isEmpty() ? null : "SOURCE: " + source,
                    date.isEmpty() ? null : "DATE: " + date,
                    snippet.isEmpty() ? null : "SUMMARY: " + snippet,
                    "URL: " + url
            ).filter(Objects::nonNull)
             .collect(Collectors.joining("\n"));
        }

    }
}

