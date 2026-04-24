package com.tcc.url_cutter_api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

public record UrlResponse(
        Long id,
        String shortUrl,
        String originalUrl,
        Long clickCount,
        LocalDateTime createdAt
) {}
