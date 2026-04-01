package com.tcc.url_cutter_api.dto.security;

import java.util.UUID;

public record AuthenticatedUser(
        UUID id,
        String email
) {}

