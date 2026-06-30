package com.sentiment.ai_worker.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record FinnhubNewsDto(
        String headline,
        String summary,
        String source,
        String url,
        Long datetime
) {}