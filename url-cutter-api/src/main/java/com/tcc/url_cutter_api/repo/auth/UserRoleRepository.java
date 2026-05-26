package com.tcc.url_cutter_api.repo.auth;

import com.tcc.url_cutter_api.model.auth.UserRole;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface UserRoleRepository
        extends ReactiveCrudRepository<UserRole, Long> {

    Flux<UserRole> findByUserId(UUID userId);

    Flux<UserRole> findByRoleId(UUID roleId);

    Flux<UserRole> findByUserIdAndRoleId(
            UUID userId,
            UUID roleId
    );

    Mono<Void> deleteByUserId(UUID userId);
}