package com.sentiment.ai_worker.Repository;

import com.sentiment.ai_worker.Entity.SentimentJob;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface SentimentJobRepository extends JpaRepository<SentimentJob, UUID> {
}