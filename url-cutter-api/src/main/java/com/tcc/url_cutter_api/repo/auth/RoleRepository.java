package com.tcc.url_cutter_api.repo.auth;

import com.tcc.url_cutter_api.enums.auth.RoleName;
import com.tcc.url_cutter_api.model.auth.Role;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface RoleRepository
        extends ReactiveCrudRepository<Role, UUID> {

    Mono<Role> findByName(RoleName name);
}