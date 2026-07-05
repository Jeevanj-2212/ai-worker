package com.sentiment.ai_worker.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sentiment.ai_worker.Entity.NewsArticle;
import com.sentiment.ai_worker.Entity.SentimentJob;
import com.sentiment.ai_worker.Repository.SentimentJobRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class AiSentimentAnalyzerService {


    private final ChatClient chatClient;
    private final SentimentJobRepository jobRepository;
    private final ObjectMapper objectMapper;

    // 2. Manual constructor so we can .build() the ChatClient
    public AiSentimentAnalyzerService(ChatClient.Builder chatClientBuilder,
                                      SentimentJobRepository jobRepository,
                                      ObjectMapper objectMapper) {
        this.chatClient = chatClientBuilder.build();
        this.jobRepository = jobRepository;
        this.objectMapper = objectMapper;
    }

    public void analyzeAndSaveSentiment(SentimentJob job, List<NewsArticle> articles) {
        StringBuilder combinedNewsTextBuilder = new StringBuilder();
        for (NewsArticle article : articles) {
            combinedNewsTextBuilder.append(article.getHeadline())
                    .append("-")
                    .append(article.getSummary())
                    .append("\n");
        }
        String combinedNewsText = combinedNewsTextBuilder.toString();
    }
}