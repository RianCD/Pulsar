package com.riancd.io.pulsar.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.boot.context.config.ConfigDataEnvironmentPostProcessor;
import org.springframework.boot.context.properties.source.ConfigurationPropertySources;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.StandardEnvironment;

import static org.assertj.core.api.Assertions.assertThat;

class ProfileConfigurationTest {
    @TempDir Path directory;

    private StandardEnvironment load(String profile, Map<String, Object> overrides) throws IOException {
        Path localFile = directory.resolve(".env");
        Files.writeString(localFile, "PULSAR_LOCAL_FILE_MARKER=fixture-only\nOLLAMA_CHAT_MODEL=local-chat\n");
        for (String suffix : new String[]{"", "-dev", "-test", "-prod"}) {
            String name = "application" + suffix + ".properties";
            try (var input = getClass().getResourceAsStream("/" + name)) {
                String content = new String(input.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
                // Resolve dev imports to an isolated fixture, never the developer's actual .env.
                content = content.replace("file:../.env", localFile.toUri().toString())
                        .replace("file:./.env", localFile.toUri().toString());
                Files.writeString(directory.resolve(name), content);
            }
        }
        Map<String, Object> settings = new HashMap<>(overrides);
        settings.put("spring.config.location", directory.toUri().toString());
        if (profile != null) settings.put("spring.profiles.active", profile);
        var environment = new StandardEnvironment();
        environment.getPropertySources().addFirst(new MapPropertySource("test-settings", settings));
        ConfigurationPropertySources.attach(environment);
        ConfigDataEnvironmentPostProcessor.applyTo(environment);
        return environment;
    }

    @Test
    void defaultDevelopmentLoadsLocalSettingsWithCollectionDisabled() throws IOException {
        var environment = load(null, Map.of());
        assertThat(environment.getDefaultProfiles()).containsExactly("dev");
        assertThat(environment.getProperty("PULSAR_LOCAL_FILE_MARKER")).isEqualTo("fixture-only");
        assertThat(environment.getProperty("pulsar.ollama.chat-model")).isEqualTo("local-chat");
        assertThat(environment.getProperty("pulsar.news.collection.enabled", Boolean.class)).isFalse();
    }

    @Test
    void productionDoesNotReadLocalSecretsOrUpdateSchema() throws IOException {
        var environment = load("prod", Map.of());
        assertThat(environment.getProperty("PULSAR_LOCAL_FILE_MARKER")).isNull();
        assertThat(environment.getProperty("spring.config.import")).isNull();
        assertThat(environment.getProperty("spring.jpa.hibernate.ddl-auto")).isEqualTo("validate");
        assertThat(environment.getProperty("spring.jpa.show-sql", Boolean.class)).isFalse();
        assertThat(environment.getProperty("pulsar.news.collection.enabled", Boolean.class)).isFalse();
    }

    @Test
    void testProfileIgnoresLocalFileAndDisablesCollection() throws IOException {
        var environment = load("test", Map.of());
        assertThat(environment.getProperty("PULSAR_LOCAL_FILE_MARKER")).isNull();
        assertThat(environment.getProperty("spring.config.import")).isNull();
        assertThat(environment.getProperty("pulsar.news.collection.enabled", Boolean.class)).isFalse();
        assertThat(environment.getProperty("mediastack.api.url")).isEqualTo("http://localhost:1/news");
    }

    @Test
    void operatorSettingsOverrideLocalFileAndDefaults() throws IOException {
        var environment = load("dev", Map.of(
                "OLLAMA_CHAT_MODEL", "operator-chat",
                "PULSAR_NEWS_COLLECTION_ENABLED", "true",
                "PULSAR_NEWS_COLLECTION_INTERVAL", "PT15M"));
        assertThat(environment.getProperty("pulsar.ollama.chat-model")).isEqualTo("operator-chat");
        assertThat(environment.getProperty("pulsar.news.collection.enabled", Boolean.class)).isTrue();
        assertThat(environment.getProperty("pulsar.news.collection.interval")).isEqualTo("PT15M");
    }
}
