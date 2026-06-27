package com.sentiment.ai_worker.Repository;

import com.sentiment.ai_worker.Entity.NewsArticle;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface NewsArticleRepository extends JpaRepository<NewsArticle, UUID> {
}