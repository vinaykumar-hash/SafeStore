package com.vaultforge.controller;

import com.vaultforge.dto.auth.AuthResponse;
import com.vaultforge.dto.auth.LoginRequest;
import com.vaultforge.dto.auth.SignupRequest;
import com.vaultforge.dto.auth.TokenRefreshRequest;
import com.vaultforge.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
  private final AuthService authService;

  public AuthController(AuthService authService) {
    this.authService = authService;
  }

  @PostMapping("/signup")
  public ResponseEntity<AuthResponse> signup(@Valid @RequestBody SignupRequest request) {
    return ResponseEntity.ok(authService.signup(request));
  }

  @PostMapping("/login")
  public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
    return ResponseEntity.ok(authService.login(request));
  }

  @PostMapping("/refresh")
  public ResponseEntity<AuthResponse> refresh(@Valid @RequestBody TokenRefreshRequest request) {
    return ResponseEntity.ok(authService.refresh(request));
  }
}
