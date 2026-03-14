package com.riancd.io.pulsar.client;

import com.riancd.io.pulsar.dto.MediastackResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class NewsApiClient {

    private final RestClient restClient;
    private final String apiKey;

    public NewsApiClient(RestClient.Builder builder,
                         @Value("${mediastack.api.url}") String apiUrl,
                         @Value("${mediastack.api.access_key}") String apiKey) {
        this.restClient = builder.baseUrl(apiUrl).build();
        this.apiKey = apiKey;
    }

    public MediastackResponse fetchRecentTechnolyNews(){
        return restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .queryParam("access_key", apiKey)
                        .queryParam("keywords", "technology")
                        .queryParam("categories", "technology")
                        .queryParam("languages", "en")
                        .queryParam("limit", 3) //puxa apenas três noticias por vez
                        .build())
                .retrieve()
                .body(MediastackResponse.class); //spring faz a conversão do JSON automaticamente para o DTO.
    }
}
