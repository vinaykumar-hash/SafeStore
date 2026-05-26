package com.vaultforge.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(
  name = "file_versions",
  indexes = {
    @Index(name = "idx_file_versions_file", columnList = "file_id")
  }
)
@Getter
@Setter
public class FileVersion {
  @Id
  @GeneratedValue
  private UUID id;

  @Column(nullable = false, name = "file_id")
  private UUID fileId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(
      name = "file_id",
      referencedColumnName = "id",
      insertable = false,
      updatable = false,
      foreignKey = @ForeignKey(name = "fk_file_versions_file")
  )
  private FileMetadata file;

  @Column(nullable = false)
  private int versionNumber;

  @Column(nullable = false)
  private String checksum;

  @Column(nullable = false, name = "created_at")
  private Instant createdAt = Instant.now();

  @Column(columnDefinition = "TEXT")
  private String snapshotMetadata;

  @Version
  private long version;
}
