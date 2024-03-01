package com.bookstore.service.impl;

import com.bookstore.dto.AuthTokenResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;

@Slf4j
@RequiredArgsConstructor
@Service
public class AuthTokenService {
    @Value("${auth0.token.url}")
    private String tokenUrl;

    @Value("${auth0.client.id}")
    private String clientId;

    @Value("${auth0.client.secret}")
    private String clientSecret;

    @Value("${spring.security.oauth2.resourceserver.jwt.audiences}")
    private String audience;

    private final RestTemplate restTemplate;

    public AuthTokenResponseDto getToken() {
        var requestBody = new HashMap<>();
        requestBody.put("client_id", clientId);
        requestBody.put("client_secret", clientSecret);
        requestBody.put("audience", audience);
        requestBody.put("grant_type", "client_credentials");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        var request = new HttpEntity<>(requestBody, headers);

        return restTemplate.postForEntity(tokenUrl, request, AuthTokenResponseDto.class)
                .getBody();
    }
}
