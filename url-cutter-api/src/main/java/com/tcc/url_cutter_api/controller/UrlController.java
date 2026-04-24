package com.tcc.url_cutter_api.controller;

import com.tcc.url_cutter_api.dto.UrlRequest;
import com.tcc.url_cutter_api.dto.UrlResponse;
import com.tcc.url_cutter_api.dto.UrlShortenResponse;
import com.tcc.url_cutter_api.model.Url;
import com.tcc.url_cutter_api.repo.ClickEventRepository;
import com.tcc.url_cutter_api.repo.UrlRepository;
import com.tcc.url_cutter_api.service.ClickEventService;
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
    public Mono<UrlShortenResponse> shorten(@RequestBody UrlRequest request) {
        return shortenerService.encode(request.getUrl())
                .map(UrlShortenResponse::new);
    }

    @GetMapping("/my-urls")
    public Flux<UrlResponse> getMyUrls() {
        return shortenerService.getAllUrls();
    }

    @DeleteMapping("/{id}")
    public Mono<Void> delete(@PathVariable Long id) {
        return shortenerService.deleteById(id);
    }


}
