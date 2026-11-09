package com.riancd.io.pulsar.config;

import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Configuration;

import static org.assertj.core.api.Assertions.assertThat;

class IntegrationPropertiesTest {
    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withUserConfiguration(PropertiesConfig.class)
            .withPropertyValues(
                    "pulsar.ollama.base-url=http://models.example.test:11434",
                    "pulsar.ollama.chat-model=custom-chat",
                    "pulsar.ollama.embedding-model=custom-embedding",
                    "pulsar.ollama.chat-timeout=PT45S",
                    "pulsar.ollama.embedding-timeout=PT15S",
                    "pulsar.ollama.temperature=0.2",
                    "mediastack.api.url=https://news.example.test/v1/news",
                    "mediastack.api.limit=8",
                    "mediastack.api.connect-timeout=PT3S",
                    "mediastack.api.read-timeout=PT12S");

    @Test
    void bindsCustomIntegrationSettings() {
        runner.run(context -> {
            assertThat(context).hasNotFailed();
            var ollama = context.getBean(OllamaProperties.class);
            assertThat(ollama.baseUrl().getHost()).isEqualTo("models.example.test");
            assertThat(ollama.chatModel()).isEqualTo("custom-chat");
            assertThat(ollama.embeddingModel()).isEqualTo("custom-embedding");
            assertThat(ollama.chatTimeout()).isEqualTo(Duration.ofSeconds(45));
            assertThat(ollama.embeddingTimeout()).isEqualTo(Duration.ofSeconds(15));
            assertThat(ollama.temperature()).isEqualTo(0.2);
            var news = context.getBean(MediastackProperties.class);
            assertThat(news.limit()).isEqualTo(8);
            assertThat(news.connectTimeout()).isEqualTo(Duration.ofSeconds(3));
            assertThat(news.readTimeout()).isEqualTo(Duration.ofSeconds(12));
        });
    }

    @Test
    void rejectsZeroTimeoutInsteadOfAllowingUnboundedRequests() {
        runner.withPropertyValues("mediastack.api.read-timeout=PT0S").run(context ->
                assertThat(context).hasFailed());
    }

    @Test
    void rejectsInvalidArticleLimit() {
        runner.withPropertyValues("mediastack.api.limit=0").run(context ->
                assertThat(context).hasFailed());
    }

    @Test
    void rejectsNegativeModelTimeout() {
        runner.withPropertyValues("pulsar.ollama.chat-timeout=-PT1S").run(context ->
                assertThat(context).hasFailed());
    }

    @Configuration(proxyBeanMethods = false)
    @EnableConfigurationProperties({OllamaProperties.class, MediastackProperties.class})
    static class PropertiesConfig { }
}
