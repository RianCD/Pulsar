package com.riancd.io.pulsar.config;

import java.net.URI;
import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.Assert;

@ConfigurationProperties("pulsar.ollama")
public record OllamaProperties(
        URI baseUrl,
        String chatModel,
        String embeddingModel,
        Duration chatTimeout,
        Duration embeddingTimeout,
        double temperature
) {
    public OllamaProperties {
        Assert.isTrue(baseUrl != null && baseUrl.getHost() != null
                && ("http".equals(baseUrl.getScheme()) || "https".equals(baseUrl.getScheme())),
                "pulsar.ollama.base-url must be an absolute HTTP(S) URL");
        Assert.hasText(chatModel, "pulsar.ollama.chat-model is required");
        Assert.hasText(embeddingModel, "pulsar.ollama.embedding-model is required");
        Assert.isTrue(chatTimeout != null && chatTimeout.compareTo(Duration.ZERO) > 0,
                "pulsar.ollama.chat-timeout must be positive");
        Assert.isTrue(embeddingTimeout != null && embeddingTimeout.compareTo(Duration.ZERO) > 0,
                "pulsar.ollama.embedding-timeout must be positive");
        Assert.isTrue(Double.isFinite(temperature) && temperature >= 0 && temperature <= 1,
                "pulsar.ollama.temperature must be between 0 and 1");
    }
}
