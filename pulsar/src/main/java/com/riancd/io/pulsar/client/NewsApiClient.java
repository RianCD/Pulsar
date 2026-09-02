package com.riancd.io.pulsar.client;

import com.riancd.io.pulsar.dto.MediastackResponse;
import com.riancd.io.pulsar.exception.InternalServerErrorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class NewsApiClient {

    private static final Logger log = LoggerFactory.getLogger(NewsApiClient.class);
    private final RestClient restClient;
    private final String apiKey;

    public NewsApiClient(RestClient.Builder builder,
                         @Value("${mediastack.api.url}") String apiUrl,
                         @Value("${mediastack.api.access_key}") String apiKey) {
        this.restClient = builder.baseUrl(apiUrl).build();
        this.apiKey = apiKey;
    }

    public MediastackResponse fetchRecentTechnolyNews(){
        log.info("Fetching recent technology news from Mediastack API...");
        try {
            MediastackResponse response = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .queryParam("access_key", apiKey)
                            .queryParam("keywords", "technology")
                            .queryParam("categories", "technology")
                            .queryParam("languages", "en")
                            .queryParam("limit", 3) //puxa apenas três noticias por vez
                            .build())
                    .retrieve()
                    .body(MediastackResponse.class); //spring faz a conversão do JSON automaticamente para o DTO.
            log.info("Successfully fetched news from Mediastack API.");
            return response;
        } catch (Exception e) {
            log.error("Failed to fetch news from Mediastack API: {}", e.getMessage(), e);
            throw new InternalServerErrorException("Failed to fetch news from Mediastack API: " + e.getMessage());
        }
    }
}
