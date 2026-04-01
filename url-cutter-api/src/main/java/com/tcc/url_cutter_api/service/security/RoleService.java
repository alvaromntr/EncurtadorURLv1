package com.tcc.url_cutter_api.service.security;

import com.tcc.url_cutter_api.enums.RoleName;
import com.tcc.url_cutter_api.model.auth.Role;
import com.tcc.url_cutter_api.repo.auth.RoleRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
public class RoleService {

    private final RoleRepository roleRepository;

    public RoleService(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    public Mono<Role> findByName(RoleName roleName) {
        return roleRepository.findByName(roleName)
                .switchIfEmpty(
                        Mono.error(
                                new IllegalStateException(
                                        "Role não encontrada: " + roleName
                                )
                        )
                );
    }

    public Mono<Role> findById(UUID roleId) {
        return roleRepository.findById(roleId)
                .switchIfEmpty(
                        Mono.error(
                                new IllegalStateException(
                                        "Role não encontrada para id: " + roleId
                                )
                        )
                );
    }
}

