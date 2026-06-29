package com.sentiment.ai_worker.kafka;

import com.sentiment.ai_worker.Entity.SentimentJob;
import com.sentiment.ai_worker.Enum.JobStatus;
import com.sentiment.ai_worker.Repository.SentimentJobRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class SentimentKafkaConsumer {

    private final SentimentJobRepository jobRepository;

    @KafkaListener(topics = "sentiment-requests", groupId = "ai-worker-group")
    public void consume(String message) {
        log.info("Received Kafka message: {}", message);


    }
}