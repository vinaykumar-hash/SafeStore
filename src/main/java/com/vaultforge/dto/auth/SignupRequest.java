package com.vaultforge.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record SignupRequest(
    @NotBlank String name,
    @Email String email,
    @NotBlank String password
) {}
