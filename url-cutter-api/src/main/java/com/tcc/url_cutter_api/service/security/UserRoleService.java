package com.tcc.url_cutter_api.service.security;

import com.tcc.url_cutter_api.controller.UrlController;
import com.tcc.url_cutter_api.model.auth.UserRole;
import com.tcc.url_cutter_api.repo.auth.UserRoleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
public class UserRoleService {

    private final UserRoleRepository userRoleRepository;

    private static final Logger logger =
            LoggerFactory.getLogger(UrlController.class);

    public UserRoleService(UserRoleRepository userRoleRepository) {
        this.userRoleRepository = userRoleRepository;
    }

    /**
     * Associa um usuário a uma role (se ainda não existir)
     */
    public Mono<UserRole> assignRole(
            UUID userId,
            UUID roleId
    ) {

        logger.info(
                "Assigning role {} to user {}",
                roleId,
                userId
        );

        return userRoleRepository
                .findByUserIdAndRoleId(userId, roleId)

                .hasElements()

                .flatMap(exists -> {

                    if (exists) {

                        logger.info(
                                "User {} already has role {}",
                                userId,
                                roleId
                        );

                        return Mono.empty();
                    }

                    UserRole userRole = new UserRole();

                    userRole.setUserId(userId);

                    userRole.setRoleId(roleId);

                    logger.info(
                            "Saving UserRole for user {} with role {}",
                            userId,
                            roleId
                    );

                    return userRoleRepository
                            .save(userRole)

                            .doOnNext(ur ->
                                    logger.info(
                                            "UserRole saved for user {} with role {}",
                                            ur.getUserId(),
                                            ur.getRoleId()
                                    )
                            )

                            .doOnError(err ->
                                    logger.error(
                                            "Erro ao salvar UserRole",
                                            err
                                    )
                            );
                });
    }

    /**
     * Lista todas as roles de um usuário
     */
    public Flux<UserRole> findByUserId(UUID userId) {
        return userRoleRepository.findByUserId(userId);
    }

    /**
     * Lista todos os usuários que possuem determinada role
     */
    public Flux<UserRole> findByRoleId(UUID roleId) {
        return userRoleRepository.findByRoleId(roleId);
    }

    /**
     * Verifica se um usuário possui uma role específica
     */
    public Mono<Boolean> hasRole(UUID userId, UUID roleId) {

        return userRoleRepository
                .findByUserIdAndRoleId(userId, roleId)
                .hasElements();
    }

    /**
     * Remove todas as roles de um usuário
     */
    public Mono<Void> deleteByUserId(UUID userId) {
        return userRoleRepository.deleteByUserId(userId);
    }
}

