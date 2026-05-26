package com.vaultforge.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Instant;
import java.util.Date;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtService {
  private final Key key;
  private final String issuer;
  private final long accessTokenTtlSeconds;
  private final long refreshTokenTtlSeconds;

  public JwtService(
      @Value("${vaultforge.security.jwt.secret}") String secret,
      @Value("${vaultforge.security.jwt.issuer}") String issuer,
      @Value("${vaultforge.security.jwt.access-token-ttl-seconds}") long accessTokenTtlSeconds,
      @Value("${vaultforge.security.jwt.refresh-token-ttl-seconds}") long refreshTokenTtlSeconds) {
    this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    this.issuer = issuer;
    this.accessTokenTtlSeconds = accessTokenTtlSeconds;
    this.refreshTokenTtlSeconds = refreshTokenTtlSeconds;
  }

  public String generateAccessToken(String subject, String role) {
    return buildToken(subject, role, accessTokenTtlSeconds);
  }

  public String generateRefreshToken(String subject, String role) {
    return buildToken(subject, role, refreshTokenTtlSeconds);
  }

  public Claims parse(String token) {
    return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
  }

  private String buildToken(String subject, String role, long ttlSeconds) {
    Instant now = Instant.now();
    return Jwts.builder()
        .setSubject(subject)
        .setIssuer(issuer)
        .claim("role", role)
        .setIssuedAt(Date.from(now))
        .setExpiration(Date.from(now.plusSeconds(ttlSeconds)))
        .signWith(key, SignatureAlgorithm.HS256)
        .compact();
  }
}
