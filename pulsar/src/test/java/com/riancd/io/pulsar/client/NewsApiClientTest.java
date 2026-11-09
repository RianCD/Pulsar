package com.riancd.io.pulsar.client;

import com.riancd.io.pulsar.config.MediastackProperties;
import java.net.URI;
import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class NewsApiClientTest {
    @Test
    void usesConfiguredEndpointAndArticleLimitWithoutExternalRequests() {
        var properties = new MediastackProperties(URI.create("https://news.example.test/custom/news"),
                7, Duration.ofSeconds(3), Duration.ofSeconds(9));
        var builder = RestClient.builder();
        var server = MockRestServiceServer.bindTo(builder).build();
        server.expect(requestTo("https://news.example.test/custom/news?access_key=test-key&keywords=technology&categories=technology&languages=en&limit=7"))
                .andExpect(queryParam("limit", "7"))
                .andRespond(withSuccess("{\"data\":[{\"title\":\"Test news\",\"description\":\"Test description\",\"source\":\"Test source\"}]}", MediaType.APPLICATION_JSON));

        var client = new NewsApiClient(builder, properties, "test-key");
        var response = client.fetchRecentTechnolyNews();

        assertThat(response.data()).hasSize(1);
        assertThat(response.data().getFirst().title()).isEqualTo("Test news");
        server.verify();
    }
}
