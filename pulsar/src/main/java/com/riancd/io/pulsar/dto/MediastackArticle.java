package com.riancd.io.pulsar.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true) //ignora campos não escolhidos
public record MediastackArticle(
        String title,
        String description,
        String source
) {
}
