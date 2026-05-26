package com.vaultforge.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Immutable;

@Entity
@Table(
  name = "audit_logs",
  indexes = {
    @Index(name = "idx_audit_user", columnList = "user_id"),
    @Index(name = "idx_audit_action", columnList = "action"),
    @Index(name = "idx_audit_timestamp", columnList = "timestamp")
  }
)
@Immutable
@Getter
@Setter
public class AuditLog {
  @Id
  @GeneratedValue
  private UUID id;

  @Column(nullable = false, updatable = false, name = "user_id")
  private UUID userId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(
      name = "user_id",
      referencedColumnName = "id",
      insertable = false,
      updatable = false,
      foreignKey = @ForeignKey(name = "fk_audit_logs_user")
  )
  private User user;

  @Column(nullable = false, updatable = false)
  private String action;

  @Column(nullable = false, updatable = false)
  private String entityType;

  @Column(nullable = false, updatable = false)
  private String entityId;

  @Column(nullable = false, updatable = false)
  private Instant timestamp = Instant.now();

  @Column(updatable = false)
  private String ipAddress;
}
