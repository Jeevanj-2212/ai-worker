package com.sentiment.ai_worker.dto;

import java.util.List;

public record SentimentAnalysisResult(
        String overallSentiment,
        Integer confidenceScore,
        List<String> keyDrivers,
        List<String> riskFactors

) {


}
