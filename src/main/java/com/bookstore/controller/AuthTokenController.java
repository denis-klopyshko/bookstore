package com.bookstore.controller;

import com.bookstore.dto.AuthTokenResponseDto;
import com.bookstore.service.impl.AuthTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/oauth/token")
public class AuthTokenController {
    private final AuthTokenService service;

    @GetMapping
    public ResponseEntity<AuthTokenResponseDto> getToken() {
        var tokenResponse = service.getToken();
        return ResponseEntity.ok(tokenResponse);
    }
}
