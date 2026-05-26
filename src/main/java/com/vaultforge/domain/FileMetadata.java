package com.vaultforge.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(
  name = "files",
  indexes = {
    @Index(name = "idx_files_owner", columnList = "owner_id"),
    @Index(name = "idx_files_filename", columnList = "filename")
  }
)
@Getter
@Setter
public class FileMetadata {
  @Id
  @GeneratedValue
  private UUID id;

  @Column(nullable = false)
  private String filename;

  @Column(nullable = false, name = "owner_id")
  private UUID ownerId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(
      name = "owner_id",
      referencedColumnName = "id",
      insertable = false,
      updatable = false,
      foreignKey = @ForeignKey(name = "fk_files_owner")
  )
  private User owner;

  @Column(nullable = false)
  private long totalSize;

  @Column(nullable = false)
  private String checksum;

  @Column(nullable = false, name = "upload_timestamp")
  private Instant uploadTimestamp = Instant.now();

  @Column
  private String tags;

  @Version
  private long version;
}
