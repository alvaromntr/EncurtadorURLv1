package com.tcc.url_cutter_api.controller;

import com.tcc.url_cutter_api.dto.ClickAnalyticsResponse;
import com.tcc.url_cutter_api.service.ClickAnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api/clicks")
@RequiredArgsConstructor
public class ClickAnalyticsController {

    private final ClickAnalyticsService analyticsService;

    @GetMapping("/url/{urlId}/analytics")
    public Flux<ClickAnalyticsResponse> analytics(
            @PathVariable Long urlId
    ) {

        return analyticsService.getAnalytics(urlId);
    }
}