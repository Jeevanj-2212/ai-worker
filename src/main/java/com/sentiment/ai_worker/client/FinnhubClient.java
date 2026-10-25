package com.sentiment.ai_worker.client;

import com.sentiment.ai_worker.dto.FinnhubNewsDto;
import com.sentiment.ai_worker.dto.NewsItem;
import com.sentiment.ai_worker.news.NewsProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

@Slf4j
@Component
public class FinnhubClient implements NewsProvider {

    private final RestClient restClient;

    @Value("${app.finnhub.api-key}")
    private String apiKey;

    public FinnhubClient(@Value("${app.finnhub.base-url}") String baseUrl) {
        // Initialize the modern Spring 6 RestClient
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .build();
    }

    @Override
    public String newsSource() {
        return "FINNHUB";
    }

    @Override
    public List<NewsItem> fetchNews(String ticker) {
        log.info("Fetching market news from Finnhub for ticker: {}", ticker);

        // Finnhub requires a date range format (YYYY-MM-DD)
        String today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
        String oneWeekAgo = LocalDate.now().minusDays(7).format(DateTimeFormatter.ISO_LOCAL_DATE);

        try {
            // 1. Call Finnhub and read the JSON into Finnhub's own shape (the DTO)
            List<FinnhubNewsDto> finhubNews = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/company-news")
                            .queryParam("symbol", ticker)
                            .queryParam("from", oneWeekAgo)
                            .queryParam("to", today)
                            .queryParam("token", apiKey)
                            .build())
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<FinnhubNewsDto>>() {});

            // 2. Guard: an empty response body gives null -> return empty list, never null
            if (finhubNews == null) {
                return Collections.emptyList();
            }

            // 3. Convert each Finnhub DTO into our neutral NewsItem
            List<NewsItem> newsItems = finhubNews.stream()
                    .filter(dto -> dto.headline() != null && !dto.headline().isBlank()) // drop articles with no headline
                    .map(dto -> new NewsItem(
                            dto.headline(),
                            dto.summary(),
                            dto.source(),
                            dto.url(),
                            dto.datetime() != null ? Instant.ofEpochSecond(dto.datetime()) : null
                    ))
                    .toList();

            log.info("Finnhub returned {} articles for ticker {}", newsItems.size(), ticker);
            return newsItems;

        } catch (Exception e) {
            log.error("Failed to fetch news from Finnhub for ticker: {}", ticker, e);
            // Return an empty list so the application flow gracefully handles the external failure
            return Collections.emptyList();
        }
    }
}
