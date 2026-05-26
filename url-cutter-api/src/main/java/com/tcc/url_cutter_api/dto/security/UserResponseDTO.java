package com.tcc.url_cutter_api.dto.security;


import com.tcc.url_cutter_api.enums.auth.UserStatus;
import com.tcc.url_cutter_api.model.auth.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import java.time.Instant;
import java.util.UUID;

public record UserResponseDTO(

        UUID id,

        String firstName,

        String lastName,

        @Email(message = "Email inválido")
        @NotBlank
        String email,

        UserStatus status,

        Instant createdAt

) {
    public static UserResponseDTO from(User user) {

        return new UserResponseDTO(

                user.getId(),

                user.getFirstName(),

                user.getLastName(),

                user.getEmail(),

                user.getStatus(),

                user.getCreatedAt()
        );
    }
}

