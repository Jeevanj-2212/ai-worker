package com.sentiment.ai_worker.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sentiment.ai_worker.Entity.NewsArticle;
import com.sentiment.ai_worker.Entity.SentimentJob;
import com.sentiment.ai_worker.Enum.JobStatus;
import com.sentiment.ai_worker.Repository.SentimentJobRepository;
import com.sentiment.ai_worker.Repository.SentimentResultRepository;
import com.sentiment.ai_worker.dto.SentimentAnalysisResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class AiSentimentAnalyzerService {


    private final ChatClient chatClient;
    private final SentimentJobRepository jobRepository;
    private final ObjectMapper objectMapper;
    private final SentimentResultRepository sentimentResultRepository;

    // 2. Manual constructor so we can .build() the ChatClient
    public AiSentimentAnalyzerService(ChatClient.Builder chatClientBuilder,
                                      SentimentJobRepository jobRepository,
                                      ObjectMapper objectMapper, SentimentResultRepository sentimentResultRepository) {
        this.chatClient = chatClientBuilder.build();
        this.jobRepository = jobRepository;
        this.objectMapper = objectMapper;
        this.sentimentResultRepository = sentimentResultRepository;
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
        SentimentAnalysisResult aiResponse = chatClient.prompt().system("You are number one  financial analyst in the world go to god mode. Analyze these articles")
                .user(combinedNewsText)
                .call()
                .entity(SentimentAnalysisResult.class);

        try {
            String jsonResult =objectMapper.writeValueAsString(aiResponse);
            job.setResult(jsonResult);
            job.setStatus(JobStatus.COMPLETED);
            job.setCompletedAt(LocalDateTime.now());
            jobRepository.save(job);
        } catch (Exception e) {
           log.error(e.getMessage(),e);
            job.setStatus(JobStatus.FAILED);
            job.setCompletedAt(LocalDateTime.now());
            jobRepository.save(job);
        }
    }
}