package com.tcc.url_cutter_api.dto;

public record ClickAnalyticsResponse(
        String date,
        Long clicks
) {
}