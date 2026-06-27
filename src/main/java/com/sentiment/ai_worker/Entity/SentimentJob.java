package com.sentiment.ai_worker.Entity;

import com.sentiment.ai_worker.Enum.JobStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "sentiment_jobs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SentimentJob {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID jobId;

    @Column(nullable = false)
    private String ticker;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private JobStatus status;

    private Integer retryCount;

    private LocalDateTime requestedAt;
    private LocalDateTime processingStarted;
    private LocalDateTime completedAt;
}