package com.vaultforge.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(
  name = "restore_jobs",
  indexes = {
    @Index(name = "idx_restore_user", columnList = "user_id")
  }
)
@Getter
@Setter
public class RestoreJob {
  @Id
  @GeneratedValue
  private UUID id;

  @Column(nullable = false, name = "user_id")
  private UUID userId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(
      name = "user_id",
      referencedColumnName = "id",
      insertable = false,
      updatable = false,
      foreignKey = @ForeignKey(name = "fk_restore_jobs_user")
  )
  private User user;

  @Column(nullable = false)
  private String status;

  @Column(nullable = false, name = "created_at")
  private Instant createdAt = Instant.now();

  @Column(nullable = false, name = "updated_at")
  private Instant updatedAt = Instant.now();

  @Version
  private long version;
}
