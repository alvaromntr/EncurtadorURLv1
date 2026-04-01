package com.tcc.url_cutter_api.repo.auth;

import com.tcc.url_cutter_api.model.auth.UserRole;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

import java.util.UUID;

public interface UserRoleRepository
        extends ReactiveCrudRepository<UserRole, Void> { // <- sem id

    Flux<UserRole> findByUserId(UUID userId);

    Flux<UserRole> findByRoleId(UUID roleId);

    @Query("SELECT * FROM user_roles WHERE user_id = :userId AND role_id = :roleId")
    Flux<UserRole> findByUserIdAndRoleId(UUID userId, UUID roleId);
}
