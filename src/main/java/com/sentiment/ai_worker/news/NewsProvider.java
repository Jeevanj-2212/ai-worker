package com.sentiment.ai_worker.news;

import com.sentiment.ai_worker.dto.NewsItem;

import java.util.List;

public interface NewsProvider {

    String newsSource();

    List<NewsItem> fetchNews(String ticker);
}
