package com.vaultforge.service;

import com.vaultforge.domain.User;
import com.vaultforge.dto.auth.AuthResponse;
import com.vaultforge.dto.auth.LoginRequest;
import com.vaultforge.dto.auth.SignupRequest;
import com.vaultforge.dto.auth.TokenRefreshRequest;
import com.vaultforge.exception.ConflictException;
import com.vaultforge.repository.UserRepository;
import com.vaultforge.security.JwtService;
import com.vaultforge.security.Role;
import com.vaultforge.service.AuditService;
import java.util.UUID;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final AuthenticationManager authenticationManager;
  private final JwtService jwtService;
  private final AuditService auditService;

  public AuthService(UserRepository userRepository,
                     PasswordEncoder passwordEncoder,
                     AuthenticationManager authenticationManager,
                     JwtService jwtService,
                     AuditService auditService) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
    this.authenticationManager = authenticationManager;
    this.jwtService = jwtService;
    this.auditService = auditService;
  }

  public AuthResponse signup(SignupRequest request) {
    userRepository.findByEmail(request.email()).ifPresent(user -> {
      throw new ConflictException("Email already registered");
    });
    User user = new User();
    user.setName(request.name());
    user.setEmail(request.email());
    user.setPassword(passwordEncoder.encode(request.password()));
    user.setRole(Role.USER);
    userRepository.save(user);
    auditService.recordUser(user.getId(), "AUTH_SIGNUP", "USER", user.getId().toString(), resolveIpAddress());
    String access = jwtService.generateAccessToken(user.getEmail(), user.getRole().name());
    String refresh = jwtService.generateRefreshToken(user.getEmail(), user.getRole().name());
    return new AuthResponse(access, refresh, "Bearer");
  }

  public AuthResponse login(LoginRequest request) {
    authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(request.email(), request.password()));
    User user = userRepository.findByEmail(request.email()).orElseThrow();
    auditService.recordUser(user.getId(), "AUTH_LOGIN", "USER", user.getId().toString(), resolveIpAddress());
    String access = jwtService.generateAccessToken(user.getEmail(), user.getRole().name());
    String refresh = jwtService.generateRefreshToken(user.getEmail(), user.getRole().name());
    return new AuthResponse(access, refresh, "Bearer");
  }

  public AuthResponse refresh(TokenRefreshRequest request) {
    var claims = jwtService.parse(request.refreshToken());
    String subject = claims.getSubject();
    String role = String.valueOf(claims.get("role"));
    userRepository.findByEmail(subject).ifPresent(user ->
        auditService.recordUser(user.getId(), "AUTH_REFRESH", "USER", user.getId().toString(), resolveIpAddress()));
    String access = jwtService.generateAccessToken(subject, role);
    String refresh = jwtService.generateRefreshToken(subject, role);
    return new AuthResponse(access, refresh, "Bearer");
  }

  private String resolveIpAddress() {
    if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attrs) {
      return attrs.getRequest().getRemoteAddr();
    }
    return "unknown";
  }
}
