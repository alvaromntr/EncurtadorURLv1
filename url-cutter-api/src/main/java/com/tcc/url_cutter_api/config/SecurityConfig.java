package com.tcc.url_cutter_api.config;

import com.tcc.url_cutter_api.service.security.JWTAuthenticationManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.AuthenticationWebFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    private final JWTAuthenticationManager authenticationManager;

    public SecurityConfig(JWTAuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {

        AuthenticationWebFilter jwtFilter = new AuthenticationWebFilter(authenticationManager);
        jwtFilter.setServerAuthenticationConverter(authenticationManager.authenticationConverter());

        return http
                .cors(Customizer.withDefaults())
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(exchanges -> exchanges
                        // libera o preflight OPTIONS
                        .pathMatchers(HttpMethod.OPTIONS).permitAll()

                        // endpoints públicos
                        .pathMatchers(ENDPOINTS_WITH_AUTHENTICATION_NOT_REQUIRED).permitAll()

                        // endpoints por role
                        //.pathMatchers(ENDPOINTS_ADMIN).hasRole("ADMIN")
                        .pathMatchers(ENDPOINTS_OPERADOR).hasAnyRole("OPERADOR", "ADMIN")

                        // endpoints que precisam de login
                        .pathMatchers(ENDPOINTS_WITH_AUTHENTICATION_REQUIRED).authenticated()

                        .anyExchange().authenticated()
                )
                .addFilterAt(jwtFilter, SecurityWebFiltersOrder.AUTHENTICATION)
                .build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(List.of("*")); // dev: qualquer origem
        configuration.setAllowedMethods(List.of("GET","POST","PUT", "PATCH", "DELETE","OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));        // importante: Authorization liberado
        configuration.setAllowCredentials(false);             // JWT não precisa
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    public static final String[] ENDPOINTS_WITH_AUTHENTICATION_NOT_REQUIRED = {
            "/auth/**",
            "/api/**",
            "/r/**"
    };

    public static final String[] ENDPOINTS_WITH_AUTHENTICATION_REQUIRED = {
            "/zzz/**"
    };

    public static final String[] ENDPOINTS_ADMIN = {

    };

    public static final String[] ENDPOINTS_OPERADOR = {
            "/zzz/**"
    };

}
