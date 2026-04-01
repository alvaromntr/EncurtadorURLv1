package com.tcc.url_cutter_api.service.security;

import com.tcc.url_cutter_api.controller.UrlController;
import com.tcc.url_cutter_api.dto.security.AuthenticatedUser;
import com.tcc.url_cutter_api.model.auth.JwtAuthenticationToken;
import com.tcc.url_cutter_api.utils.JWTUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.server.authentication.ServerAuthenticationConverter;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class JWTAuthenticationManager implements ReactiveAuthenticationManager {

    private final JWTUtil jwtUtil;

    private final UserService userService;

    private UserRoleService userRoleService;

    private RoleService roleService;

    private static final Logger logger =
            LoggerFactory.getLogger(UrlController.class);

    public JWTAuthenticationManager(JWTUtil jwtUtil, UserService userService, UserRoleService userRoleService, RoleService roleService) {
        this.jwtUtil = jwtUtil;
        this.userService = userService;
        this.userRoleService = userRoleService;
        this.roleService = roleService;
    }

    @Override
    public Mono<Authentication> authenticate(Authentication authentication)
            throws AuthenticationException {

        String token = authentication.getCredentials().toString();
        String email = jwtUtil.extractEmail(token);

        System.out.println("QQQQQQQQQQQQQQQQQQQQQQQQQQQ");

        logger.info("JWT token received");
        logger.info("Email extracted from JWT: {}", email);

        return userService.findByEmail(email)
                .switchIfEmpty(Mono.error(
                        new BadCredentialsException("User not found")
                ))
                .flatMap(user -> {

                    if (!jwtUtil.validateToken(token, user.getEmail())) {
                        return Mono.error(
                                new BadCredentialsException("Invalid JWT token")
                        );
                    }

                    AuthenticatedUser principal = new AuthenticatedUser(
                            user.getId(),
                            user.getEmail()
                    );

                    return userRoleService.findByUserId(user.getId())
                            .flatMap(userRole ->
                                    roleService.findById(userRole.getRoleId())
                            )
                            .map(role ->
                                    new SimpleGrantedAuthority(
                                            "ROLE_" + role.getName().name()
                                    )
                            )
                            .collectList()
                            .map(authorities ->
                                    new JwtAuthenticationToken(
                                            principal,
                                            token,
                                            authorities
                                    )
                            );
                });
    }




    public ServerAuthenticationConverter authenticationConverter() {
        return new ServerAuthenticationConverter() {
            @Override
            public Mono<Authentication> convert(ServerWebExchange exchange) {
                String token = exchange.getRequest().getHeaders().getFirst("Authorization");
                if (token != null && token.startsWith("Bearer ")) {
                    token = token.substring(7);
                    return Mono.just(
                            new UsernamePasswordAuthenticationToken(null, token)
                    );
                }
                return Mono.empty();
            }
        };
    }
}
