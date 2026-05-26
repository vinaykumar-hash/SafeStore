package com.vaultforge.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(
  name = "upload_jobs",
  indexes = {
    @Index(name = "idx_upload_user", columnList = "user_id"),
    @Index(name = "idx_upload_file", columnList = "file_id")
  }
)
@Getter
@Setter
public class UploadJob {
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
      foreignKey = @ForeignKey(name = "fk_upload_jobs_user")
    )
    private User user;

    @Column(nullable = false, name = "file_id")
    private UUID fileId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
      name = "file_id",
      referencedColumnName = "id",
      insertable = false,
      updatable = false,
      foreignKey = @ForeignKey(name = "fk_upload_jobs_file")
    )
    private FileMetadata file;

  @Column(nullable = false)
  private String status;

  @Column(nullable = false, name = "created_at")
  private Instant createdAt = Instant.now();

  @Column(nullable = false, name = "updated_at")
  private Instant updatedAt = Instant.now();

  @Version
  private long version;
}
