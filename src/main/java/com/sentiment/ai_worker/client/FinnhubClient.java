package com.sentiment.ai_worker.client;

import com.sentiment.ai_worker.dto.FinnhubNewsDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

@Slf4j
@Component
public class FinnhubClient {

    private final RestClient restClient;

    @Value("${app.finnhub.api-key}")
    private String apiKey;

    public FinnhubClient(@Value("${app.finnhub.base-url}") String baseUrl) {
        // Initialize the modern Spring 6 RestClient
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .build();
    }

    public List<FinnhubNewsDto> fetchLatestNews(String ticker) {
        log.info("Fetching market news from Finnhub for ticker: {}", ticker);

        // Finnhub requires a date range format (YYYY-MM-DD)
        String today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
        String oneWeekAgo = LocalDate.now().minusDays(7).format(DateTimeFormatter.ISO_LOCAL_DATE);

        try {
            return restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/company-news")
                            .queryParam("symbol", ticker)
                            .queryParam("from", oneWeekAgo)
                            .queryParam("to", today)
                            .queryParam("token", apiKey)
                            .build())
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<FinnhubNewsDto>>() {});
        } catch (Exception e) {
            log.error("Failed to fetch news from Finnhub for ticker: {}", ticker, e);
            // Return an empty list so the application flow gracefully handles the external failure
            return Collections.emptyList();
        }
    }
}