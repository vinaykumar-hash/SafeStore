package com.vaultforge.dto.auth;

public record AuthResponse(String accessToken, String refreshToken, String tokenType) {}
