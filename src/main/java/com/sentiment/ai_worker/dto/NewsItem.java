package com.sentiment.ai_worker.dto;

import java.time.Instant;

public record NewsItem(
        String headline,
        String summary,
        String source,
        String url,
        Instant publishedAt) {
}
