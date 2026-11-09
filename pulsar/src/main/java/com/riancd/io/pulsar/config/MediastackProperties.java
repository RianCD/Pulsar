package com.riancd.io.pulsar.config;

import java.net.URI;
import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.Assert;

@ConfigurationProperties("mediastack.api")
public record MediastackProperties(URI url, int limit, Duration connectTimeout, Duration readTimeout) {
    public MediastackProperties {
        Assert.isTrue(url != null && url.getHost() != null
                && ("http".equals(url.getScheme()) || "https".equals(url.getScheme())),
                "mediastack.api.url must be an absolute HTTP(S) URL");
        Assert.isTrue(limit >= 1 && limit <= 100, "mediastack.api.limit must be between 1 and 100");
        Assert.isTrue(connectTimeout != null && connectTimeout.compareTo(Duration.ZERO) > 0,
                "mediastack.api.connect-timeout must be positive");
        Assert.isTrue(readTimeout != null && readTimeout.compareTo(Duration.ZERO) > 0,
                "mediastack.api.read-timeout must be positive");
    }
}
