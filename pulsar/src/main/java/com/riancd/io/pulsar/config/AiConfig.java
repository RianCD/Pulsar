package com.riancd.io.pulsar.config;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.model.ollama.OllamaEmbeddingModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

import java.time.Duration;

@Configuration
public class AiConfig {

    // Porta padrão onde o contêiner do Ollama está rodando
    private static final String OLLAMA_URL = "http://localhost:11434";

    @Bean
    public ChatLanguageModel chatLanguageModel() {
        return OllamaChatModel.builder()
                .baseUrl(OLLAMA_URL)
                .modelName("llama3")
                .timeout(Duration.ofMinutes(2))
                // Temperature define a criatividade (0.0 a 1.0).
                .temperature(0.3)
                .build();
    }

    @Bean
    public EmbeddingModel embeddingModel() {
        return OllamaEmbeddingModel.builder()
                .baseUrl(OLLAMA_URL)
                .modelName("all-minilm")
                .timeout(Duration.ofMinutes(2))
                .build();
    }

    @Bean
    public RestClient.Builder restClientBuilder() {
        return RestClient.builder();
    }
}