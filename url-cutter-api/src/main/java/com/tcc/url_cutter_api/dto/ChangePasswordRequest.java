package com.tcc.url_cutter_api.dto;

import jakarta.validation.constraints.NotBlank;

public record ChangePasswordRequest(

        @NotBlank
        String currentPassword,

        @NotBlank
        String newPassword
) {}
