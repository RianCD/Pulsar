package com.riancd.io.pulsar.dto;

public record NewsResponse(
        Long id,
        String title,
        String summary,
        String sentiment
) {
}
