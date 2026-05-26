package com.tcc.url_cutter_api.repo.auth;

import com.tcc.url_cutter_api.model.auth.User;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface UserRepository
        extends ReactiveCrudRepository<User, UUID> {

    Mono<User> findById(UUID id);

    Mono<User> findByEmail(String email);

    Mono<Boolean> existsByEmail(String email);

    Mono<Void> deleteById(UUID id);
}

