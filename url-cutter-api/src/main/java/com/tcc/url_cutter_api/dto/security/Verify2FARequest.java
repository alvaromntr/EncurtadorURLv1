package com.tcc.url_cutter_api.dto.security;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record Verify2FARequest(
        @Email(message = "Email inválido")
        @NotBlank
        String email,
        String code
) {}