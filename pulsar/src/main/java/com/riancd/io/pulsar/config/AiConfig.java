package com.riancd.io.pulsar.config;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.model.ollama.OllamaEmbeddingModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@Configuration
@EnableConfigurationProperties(OllamaProperties.class)
public class AiConfig {

    @Bean
    public ChatLanguageModel chatLanguageModel(OllamaProperties properties) {
        return OllamaChatModel.builder()
                .baseUrl(properties.baseUrl().toString())
                .modelName(properties.chatModel())
                .timeout(properties.chatTimeout())
                .temperature(properties.temperature())
                .build();
    }

    @Bean
    public EmbeddingModel embeddingModel(OllamaProperties properties) {
        return OllamaEmbeddingModel.builder()
                .baseUrl(properties.baseUrl().toString())
                .modelName(properties.embeddingModel())
                .timeout(properties.embeddingTimeout())
                .build();
    }

}
