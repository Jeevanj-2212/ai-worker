package com.sentiment.ai_worker.kafka;

import com.sentiment.ai_worker.Entity.NewsArticle;
import com.sentiment.ai_worker.Entity.SentimentJob;
import com.sentiment.ai_worker.Enum.JobStatus;
import com.sentiment.ai_worker.Repository.NewsArticleRepository;
import com.sentiment.ai_worker.Repository.SentimentJobRepository;
import com.sentiment.ai_worker.client.FinnhubClient;
import com.sentiment.ai_worker.dto.FinnhubNewsDto;
import com.sentiment.ai_worker.service.AiSentimentAnalyzerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Slf4j
@Service
@RequiredArgsConstructor
public class SentimentKafkaConsumer {

    private final SentimentJobRepository jobRepository;
    private final ObjectMapper objectMapper;
    private final StringRedisTemplate redisTemplate;

    private final FinnhubClient finhubClient;
    private final NewsArticleRepository newsArticleRepository;
    private final AiSentimentAnalyzerService aiSentimentAnalyzerService;

    @KafkaListener(topics = "sentiment-tasks-v1", groupId = "super-flush-group-1")
    public void consume(String message) {
        System.out.println(" CONSUMER CALLED WITH: " + message);
        log.info("Received Kafka message: {}", message);
        SentimentJob job = null;
        try {
            // 1. Parse the JSON string
            JsonNode jsonNode = objectMapper.readTree(message);
            UUID jobId = UUID.fromString(jsonNode.get("jobId").asText());
            String ticker = jsonNode.get("ticker").asText();

            // 2. Fetch the job from the database

            job = jobRepository.findById(jobId)
                    .orElseThrow(() -> new IllegalArgumentException("Job not found in DB: " + jobId));

            // 3. Update the state to PROCESSING
            job.setStatus(JobStatus.PROCESSING);
            job.setProcessingStarted(LocalDateTime.now()); // Mark exactly when we started

            // 4. Save the lock into PostgreSQL
            jobRepository.save(job);

            log.info("Successfully locked Job {} for ticker {}. Status is now PROCESSING.", jobId, ticker);

            // Mapping FinhubNewsDto to NewsArticle
            // use case of dto

            List<FinnhubNewsDto> newsDtos = finhubClient.fetchLatestNews(ticker);

            SentimentJob finalJob = job;
            List<NewsArticle> articlesToSave = newsDtos.stream()
                    .limit(5)
                    .map(dto -> NewsArticle.builder()
                            .job(finalJob) // Attach the locked PostgreSQL job!
                            .headline(dto.headline())
                            .summary(dto.summary())
                            .provider(dto.source())
                            .url(dto.url())
                            .build())
                    .toList();
            newsArticleRepository.saveAll(articlesToSave);
            log.info("Successfully saved {} articles to the database for job {}.", articlesToSave.size(), jobId);
            String aiResult =  aiSentimentAnalyzerService.analyzeAndSaveSentiment(job, articlesToSave);
            String redisKey = "Sentiment:" + ticker.toUpperCase();

            log.info("Caching sentiment results in Redis for ticker: {} with a 10-minute TTL", ticker);
            redisTemplate.opsForValue().set(
                    redisKey,
                    aiResult != null ? aiResult : "COMPLETED_WITHOUT_RAW_STRING",
                    Duration.ofMinutes(10) );


        } catch (Exception e) {

            log.error("Fatal error processing Kafka message: {}", message, e);
            // In a production system, we would route this to a Dead Letter Queue (DLQ) here
            if (job != null) {
                job.setStatus(JobStatus.FAILED);
                jobRepository.save(job);
            }

        }
    }
    }
