package com.bookstore.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record AuthTokenResponseDto(String access_token, Long expires_in, String token_type, String scope) {
}
