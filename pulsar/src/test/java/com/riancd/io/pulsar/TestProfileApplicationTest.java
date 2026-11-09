package com.riancd.io.pulsar;

import com.riancd.io.pulsar.client.NewsApiClient;
import com.riancd.io.pulsar.repository.NewsRepository;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import javax.sql.DataSource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.ScheduledAnnotationBeanPostProcessor;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
class TestProfileApplicationTest {
    @MockBean private NewsRepository repository;
    @MockBean private NewsApiClient apiClient;
    @MockBean private ChatLanguageModel chatModel;
    @MockBean private EmbeddingModel embeddingModel;
    @Autowired private ApplicationContext context;
    @Autowired private Environment environment;
    @Autowired private MockMvc mvc;

    @Test
    void startsWithoutDatabaseLocalSecretsOrScheduledExternalCalls() {
        assertThat(context.getBeansOfType(DataSource.class)).isEmpty();
        assertThat(context.getBeansOfType(ScheduledAnnotationBeanPostProcessor.class)).isEmpty();
        assertThat(environment.getProperty("spring.config.import")).isNull();
        assertThat(environment.getProperty("pulsar.news.collection.enabled", Boolean.class)).isFalse();
        verifyNoInteractions(repository, apiClient, chatModel, embeddingModel);
    }

    @Test
    void newsListingRemainsAvailableWithCollectionDisabled() throws Exception {
        mvc.perform(get("/api/news"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
        verifyNoInteractions(apiClient, chatModel, embeddingModel);
    }
}
