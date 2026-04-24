package com.tcc.url_cutter_api.utils;

import com.tcc.url_cutter_api.dto.security.AuthenticatedUser;
import com.tcc.url_cutter_api.model.auth.JwtAuthenticationToken;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
public class SecurityUtils {

    /**
     * Retorna o usuário autenticado atual
     */
    public Mono<AuthenticatedUser> getCurrentUser() {
        return ReactiveSecurityContextHolder.getContext()
                .map(ctx -> ctx.getAuthentication())
                .cast(JwtAuthenticationToken.class)
                .map(auth -> (AuthenticatedUser) auth.getPrincipal());
    }

    /**
     * Retorna apenas o ID do usuário autenticado
     */
    public Mono<UUID> getCurrentUserId() {
        return getCurrentUser()
                .map(AuthenticatedUser::id)
                .switchIfEmpty(Mono.error(new RuntimeException("User not authenticated")));
    }

    /**
     * Retorna o email do usuário autenticado
     */
    public Mono<String> getCurrentUserEmail() {
        return getCurrentUser()
                .map(AuthenticatedUser::email);
    }

    /**
     * Verifica se o usuário está autenticado
     */
    public Mono<Boolean> isAuthenticated() {
        return ReactiveSecurityContextHolder.getContext()
                .map(ctx -> ctx.getAuthentication())
                .map(auth -> auth != null && auth.isAuthenticated());
    }
}