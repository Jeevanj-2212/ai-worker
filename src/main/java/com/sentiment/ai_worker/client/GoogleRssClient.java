package com.sentiment.ai_worker.client;

import com.sentiment.ai_worker.dto.NewsItem;
import com.sentiment.ai_worker.news.NewsProvider;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Collections;
import java.util.List;

@Component
@Slf4j
public class GoogleRssClient implements NewsProvider {

    private static final String BASE_URL = "https://news.google.com/rss/search";
    private static final int MAX_ARTICLES = 10;
    private static final int TIMEOUT_MS = 5000;

    @Override
    public String newsSource() {
        return "GOOGLE RSS";
    }

    @Override
    public List<NewsItem> fetchNews(String ticker) {
        log.info("Fetching news from Google RSS for ticker: {}", ticker);

        // 1. Build the search URL. Encode the search words (spaces, '&' in tickers like M&M)
        String query = ticker + " share NSE when:7d";
        String url = BASE_URL
                + "?q=" + URLEncoder.encode(query, StandardCharsets.UTF_8)
                + "&hl=en-IN&gl=IN&ceid=IN:en";

        try {
            // 2. Download + parse as XML (the HTML parser would drop the text of <link>)
            Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (compatible; SentimentEngine/1.0)")
                    .timeout(TIMEOUT_MS)
                    .parser(Parser.xmlParser())
                    .get();

            // 3. Every <item> is one article
            Elements items = doc.select("item");
            log.info("Google RSS returned {} items for ticker {}", items.size(), ticker);

            // 4. Convert each <item> to our neutral NewsItem
            return items.stream()
                    .map(this::toNewsItem)
                    .filter(news -> news.headline() != null && !news.headline().isBlank())
                    .limit(MAX_ARTICLES)
                    .toList();

        } catch (Exception e) {
            log.error("Failed to fetch news from Google RSS for ticker: {}", ticker, e);
            return Collections.emptyList();   // never null
        }
    }

    // ───────────── helpers ─────────────

    /** Maps one <item> to a NewsItem using our mapping table. */
    private NewsItem toNewsItem(Element item) {
        return new NewsItem(
                cleanHeadline(textOf(item, "title")),   // headline without " - Publisher"
                null,                                   // Google gives no real summary
                textOf(item, "source"),                 // publisher name, e.g. "Economic Times"
                textOf(item, "link"),
                parseDate(textOf(item, "pubDate"))
        );
    }

    /** Text of the first child tag, or null if the tag is missing. */
    private String textOf(Element item, String tag) {
        Element element = item.selectFirst(tag);
        return element != null ? element.text() : null;
    }

    /** "TCS Share Price Today - TechGraph"  ->  "TCS Share Price Today" */
    private String cleanHeadline(String title) {
        if (title == null) {
            return null;
        }
        int lastDash = title.lastIndexOf(" - ");
        return lastDash > 0 ? title.substring(0, lastDash).trim() : title.trim();
    }

    /** "Thu, 22 Oct 2026 07:00:00 GMT" -> Instant. Bad/missing date -> null (don't fail the whole list). */
    private Instant parseDate(String pubDate) {
        if (pubDate == null || pubDate.isBlank()) {
            return null;
        }
        try {
            return ZonedDateTime.parse(pubDate, DateTimeFormatter.RFC_1123_DATE_TIME).toInstant();
        } catch (DateTimeParseException e) {
            log.warn("Could not parse pubDate '{}'", pubDate);
            return null;
        }
    }
}
