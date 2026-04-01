package com.tcc.url_cutter_api.dto.security;


public record AuthResponseRecord(String token, String role, UserResponseDTO userResponseDTO) {
}
