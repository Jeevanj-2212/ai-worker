package com.sentiment.ai_worker.Repository;

import com.sentiment.ai_worker.Entity.SentimentResult;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface SentimentResultRepository extends JpaRepository<SentimentResult, UUID> {
}