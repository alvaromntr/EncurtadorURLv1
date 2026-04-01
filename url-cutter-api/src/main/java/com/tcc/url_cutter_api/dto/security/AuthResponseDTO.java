package com.tcc.url_cutter_api.dto.security;

public record AuthResponseDTO(
        String token,
        String role,
        UserResponseDTO userResponseDTO
) {}
