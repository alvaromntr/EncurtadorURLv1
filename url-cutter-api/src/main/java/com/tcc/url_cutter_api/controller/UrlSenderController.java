package com.tcc.url_cutter_api.controller;

import com.tcc.url_cutter_api.repo.UrlRepository;
import com.tcc.url_cutter_api.service.ClickEventService;
import com.tcc.url_cutter_api.service.SimpleURLShortenerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.net.URI;

@RestController
@RequiredArgsConstructor
public class UrlSenderController {

    private final SimpleURLShortenerService shortenerService;

    private final ClickEventService clickEventService;

    private final UrlRepository urlRepository;

    @GetMapping("/r/{shortCode}")
    public Mono<ResponseEntity<Object>> redirect(
            @PathVariable String shortCode,
            ServerHttpRequest request
    ) {

        return urlRepository.findByShortCode(shortCode)
                .flatMap(url -> {

                    String ip = request.getRemoteAddress() != null
                            ? request.getRemoteAddress().getAddress().getHostAddress()
                            : "unknown";

                    String userAgent = request.getHeaders().getFirst("User-Agent");

                    return clickEventService
                            .registerClick(url.getId(), ip, userAgent)
                            .thenReturn(
                                    ResponseEntity
                                            .status(302)
                                            .location(URI.create(url.getOriginalUrl()))
                                            .build()
                            );
                })
                .switchIfEmpty(
                        Mono.just(ResponseEntity.notFound().build())
                );
    }


}
