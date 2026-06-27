package com.sentiment.ai_worker.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "news_articles")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NewsArticle {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // FetchType.LAZY is a massive performance boost. It means when you query a NewsArticle,
    // Hibernate won't automatically execute a second query to fetch the entire SentimentJob unless you explicitly ask for it.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_id", nullable = false)
    private SentimentJob job;

    @Column(nullable = false)
    private String headline;

    @Column(columnDefinition = "TEXT")
    private String summary;

    private String provider;

    @Column(length = 512) // URLs can sometimes be longer than the default 255 varchar limit
    private String url;
}