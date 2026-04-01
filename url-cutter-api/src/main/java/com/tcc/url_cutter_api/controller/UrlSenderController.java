package com.tcc.url_cutter_api.controller;

import com.tcc.url_cutter_api.dto.UrlRequest;
import com.tcc.url_cutter_api.dto.UrlResponse;
import com.tcc.url_cutter_api.repo.UrlRepository;
import com.tcc.url_cutter_api.service.SimpleURLShortenerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.net.URI;

@RestController
@RequiredArgsConstructor
public class UrlSenderController {

    private final SimpleURLShortenerService shortenerService;

    private final UrlRepository urlRepository;

    // 🔗 Endpoint da URL curta
    @GetMapping("/r/{shortCode}")
    public Mono<ResponseEntity<Object>> redirect(@PathVariable String shortCode) {

        return urlRepository.findByShortCode(shortCode)
                .map(url ->
                        ResponseEntity
                                .status(302) // HTTP Redirect
                                .location(URI.create(url.getOriginalUrl()))
                                .build()
                )
                .switchIfEmpty(
                        Mono.just(ResponseEntity.<Void>notFound().build())
                );

    }


}
