package com.vaultforge.domain;

import com.vaultforge.security.Role;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(
  name = "users",
  indexes = {
    @Index(name = "idx_users_email", columnList = "email", unique = true)
  }
)
@Getter
@Setter
public class User {
  @Id
  @GeneratedValue
  private UUID id;

  @Column(nullable = false)
  private String name;

  @Column(nullable = false, unique = true)
  private String email;

  @Column(nullable = false)
  private String password;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private Role role;

  @Column(nullable = false, name = "created_at")
  private Instant createdAt = Instant.now();

  @Column(nullable = false, name = "updated_at")
  private Instant updatedAt = Instant.now();

  @Version
  private long version;
}
