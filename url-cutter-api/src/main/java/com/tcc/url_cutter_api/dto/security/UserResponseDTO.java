package com.tcc.url_cutter_api.dto.security;

import com.tcc.url_cutter_api.enums.UserStatus;
import com.tcc.url_cutter_api.model.auth.User;

import java.time.Instant;
import java.util.UUID;

public record UserResponseDTO(
        UUID id,
        String email,
        UserStatus status,
        Instant createdAt
) {
    public static UserResponseDTO from(User user) {
        return new UserResponseDTO(
                user.getId(),
                user.getEmail(),
                user.getStatus(),
                user.getCreatedAt()
        );
    }
}

