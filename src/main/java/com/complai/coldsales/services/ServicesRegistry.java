package com.complai.coldsales.services;

import com.complai.coldsales.config.Settings;
import lombok.Getter;
import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Centralized factory/registry for deterministic services so they can be shared and
 * consistently configured across the application and tests.
 */
@Getter
public final class ServicesRegistry implements AutoCloseable {

    private static final Logger log = LoggerFactory.getLogger(ServicesRegistry.class);

    private final EmailService emailService;
    private final WebScraperService webScraperService;
    private final LinkedInScraperService linkedInScraperService;
    private final NewsSearchService newsSearchService;

    private final ExecutorService webScrapingExecutor;
    private final ExecutorService linkedInExecutor;
    private final ExecutorService newsExecutor;
    private final OkHttpClient httpClient;

    private final boolean ownsWebScrapingExecutor;
    private final boolean ownsLinkedInExecutor;
    private final boolean ownsNewsExecutor;
    private final boolean ownsHttpClient;

    private ServicesRegistry(Builder builder) {
        Settings settings = builder.settings;

        log.info("🔧 Initializing ServicesRegistry...");

        this.webScrapingExecutor = builder.webScrapingExecutor != null
                ? builder.webScrapingExecutor
                : createExecutor("web-scraper-%d", 5);
        this.linkedInExecutor = builder.linkedInExecutor != null
                ? builder.linkedInExecutor
                : createExecutor("linkedin-scraper-%d", 3);
        this.newsExecutor = builder.newsExecutor != null
                ? builder.newsExecutor
                : createExecutor("news-search-%d", 3);

        this.ownsWebScrapingExecutor = builder.webScrapingExecutor == null;
        this.ownsLinkedInExecutor = builder.linkedInExecutor == null;
        this.ownsNewsExecutor = builder.newsExecutor == null;

        if (this.ownsWebScrapingExecutor) {
            log.debug("Created web scraping executor (5 threads)");
        }
        if (this.ownsLinkedInExecutor) {
            log.debug("Created LinkedIn scraping executor (3 threads)");
        }
        if (this.ownsNewsExecutor) {
            log.debug("Created news search executor (3 threads)");
        }

        this.httpClient = builder.httpClient != null
                ? builder.httpClient
                : createHttpClient();
        this.ownsHttpClient = builder.httpClient == null;

        if (this.ownsHttpClient) {
            log.debug("Created shared HTTP client with connection pooling");
        }

        this.emailService = builder.emailService != null ? builder.emailService : new EmailService(requireSettings(settings));
        this.webScraperService = builder.webScraperService != null ? builder.webScraperService : new WebScraperService(webScrapingExecutor, httpClient);
        this.linkedInScraperService = builder.linkedInScraperService != null ? builder.linkedInScraperService : new LinkedInScraperService(linkedInExecutor, httpClient);
        this.newsSearchService = builder.newsSearchService != null ? builder.newsSearchService : new NewsSearchService(newsExecutor, httpClient);

        log.info("✅ ServicesRegistry initialized successfully");
    }

    private ExecutorService createExecutor(String nameFormat, int threads) {
        return Executors.newFixedThreadPool(threads, namedThreadFactory(nameFormat));
    }

    private ThreadFactory namedThreadFactory(String pattern) {
        AtomicInteger counter = new AtomicInteger(1);
        return runnable -> {
            Thread thread = new Thread(runnable);
            thread.setName(String.format(pattern, counter.getAndIncrement()));
            thread.setDaemon(true);
            return thread;
        };
    }

    private Settings requireSettings(Settings settings) {
        return Objects.requireNonNull(settings, "Settings must be provided when EmailService is not supplied");
    }

    public static ServicesRegistry fromSettings(Settings settings) {
        return builder().settings(settings).build();
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public void close() {
        log.info("🔄 Shutting down ServicesRegistry...");
        shutdownExecutor(webScrapingExecutor, ownsWebScrapingExecutor);
        shutdownExecutor(linkedInExecutor, ownsLinkedInExecutor);
        shutdownExecutor(newsExecutor, ownsNewsExecutor);
        closeHttpClient();
        log.info("✅ ServicesRegistry shutdown complete");
    }

    private void shutdownExecutor(ExecutorService executor, boolean shouldShutdown) {
        if (!shouldShutdown || executor == null) {
            return;
        }
        String executorName = executor.toString();
        log.debug("Shutting down executor: {}", executorName);
        executor.shutdown();
        try {
            if (!executor.awaitTermination(2, TimeUnit.SECONDS)) {
                log.warn("Executor did not terminate within 2 seconds, forcing shutdown: {}", executorName);
                executor.shutdownNow();
            } else {
                log.debug("Executor terminated successfully: {}", executorName);
            }
        } catch (InterruptedException e) {
            log.warn("Interrupted while waiting for executor termination: {}", executorName);
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    private OkHttpClient createHttpClient() {
        return new OkHttpClient.Builder()
                .connectionPool(new ConnectionPool(10, 5, TimeUnit.MINUTES))
                .build();
    }

    private void closeHttpClient() {
        if (!ownsHttpClient || httpClient == null) {
            return;
        }
        log.debug("Closing HTTP client and connection pool...");
        httpClient.dispatcher().executorService().shutdown();
        httpClient.connectionPool().evictAll();
        if (httpClient.cache() != null) {
            try {
                httpClient.cache().close();
                log.debug("HTTP cache closed successfully");
            } catch (IOException e) {
                log.warn("Error closing HTTP cache: {}", e.getMessage());
            }
        }
    }

    public static final class Builder {
        private Settings settings;
        private EmailService emailService;
        private WebScraperService webScraperService;
        private LinkedInScraperService linkedInScraperService;
        private NewsSearchService newsSearchService;

        private ExecutorService webScrapingExecutor;
        private ExecutorService linkedInExecutor;
        private ExecutorService newsExecutor;
        private OkHttpClient httpClient;

        private Builder() {}

        public Builder settings(Settings settings) {
            this.settings = settings;
            return this;
        }

        public Builder emailService(EmailService emailService) {
            this.emailService = emailService;
            return this;
        }

        public Builder webScraperService(WebScraperService webScraperService) {
            this.webScraperService = webScraperService;
            return this;
        }

        public Builder linkedInScraperService(LinkedInScraperService linkedInScraperService) {
            this.linkedInScraperService = linkedInScraperService;
            return this;
        }

        public Builder newsSearchService(NewsSearchService newsSearchService) {
            this.newsSearchService = newsSearchService;
            return this;
        }

        public Builder webScrapingExecutor(ExecutorService executorService) {
            this.webScrapingExecutor = executorService;
            return this;
        }

        public Builder linkedInExecutor(ExecutorService executorService) {
            this.linkedInExecutor = executorService;
            return this;
        }

        public Builder newsExecutor(ExecutorService executorService) {
            this.newsExecutor = executorService;
            return this;
        }

        public Builder httpClient(OkHttpClient httpClient) {
            this.httpClient = httpClient;
            return this;
        }

        public ServicesRegistry build() {
            return new ServicesRegistry(this);
        }
    }
}

