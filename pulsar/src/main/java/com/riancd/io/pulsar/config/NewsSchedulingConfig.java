package com.riancd.io.pulsar.config;

import com.riancd.io.pulsar.service.NewsService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@Configuration(proxyBeanMethods = false)
@EnableScheduling
@ConditionalOnProperty(name = "pulsar.news.collection.enabled", havingValue = "true")
public class NewsSchedulingConfig {
    private final NewsService newsService;

    public NewsSchedulingConfig(NewsService newsService) {
        this.newsService = newsService;
    }

    @Scheduled(
            fixedRateString = "${pulsar.news.collection.interval}",
            initialDelayString = "${pulsar.news.collection.initial-delay}"
    )
    public void collectNews() {
        newsService.fetchAndProcessNews();
    }
}
