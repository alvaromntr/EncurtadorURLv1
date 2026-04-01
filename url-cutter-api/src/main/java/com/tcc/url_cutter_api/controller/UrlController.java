package com.tcc.url_cutter_api.controller;

import com.tcc.url_cutter_api.dto.UrlRequest;
import com.tcc.url_cutter_api.dto.UrlResponse;
import com.tcc.url_cutter_api.model.Url;
import com.tcc.url_cutter_api.repo.UrlRepository;
import com.tcc.url_cutter_api.service.SimpleURLShortenerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class UrlController {

    private final SimpleURLShortenerService shortenerService;

    private final UrlRepository urlRepository;

    @PostMapping("/shorten")
    public Mono<UrlResponse> shorten(@RequestBody UrlRequest request) {
        return shortenerService.encode(request.getUrl())
                .map(UrlResponse::new);
    }

    @GetMapping("/all")
    public Flux<Url> getAll() {
        return urlRepository.findAll()
                .doOnNext(u -> System.out.println("DO BANCO: " + u));
    }


}
