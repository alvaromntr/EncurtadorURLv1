package com.tcc.url_cutter_api.dto.security;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import java.util.UUID;

public record AuthenticatedUser(
        UUID id,
        @Email(message = "Email inválido")
        @NotBlank
        String email
) {}

