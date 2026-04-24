package com.tcc.url_cutter_api.repo;

import com.tcc.url_cutter_api.model.Url;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;


public interface UrlRepository extends ReactiveCrudRepository<Url, Long> {

    Mono<Url> findByShortCode(String shortCode);

    Mono<Url> findByOriginalUrl(String originalUrl);

    Mono<Boolean> existsByShortCode(String shortCode);

    Flux<Url> findByUserId(UUID userId);
}