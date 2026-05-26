package com.tcc.url_cutter_api.repo.auth;

import com.tcc.url_cutter_api.model.auth.TwoFactorCode;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface TwoFactorCodeRepository
        extends ReactiveCrudRepository<TwoFactorCode, UUID> {

    Mono<TwoFactorCode>
    findTopByUserIdOrderByExpiresAtDesc(UUID userId);

    Mono<Void> deleteByUserId(UUID userId);
}