package com.vaultforge.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(
  name = "chunks",
  indexes = {
    @Index(name = "idx_chunks_hash", columnList = "hash", unique = true),
    @Index(name = "idx_chunks_storage_path", columnList = "storage_path")
  }
)
@Getter
@Setter
public class Chunk {
  @Id
  @GeneratedValue
  private UUID id;

  @Column(nullable = false, unique = true)
  private String hash;

  @Column(nullable = false, name = "storage_path")
  private String storagePath;

  @Column(nullable = false)
  private long size;

  @Column(nullable = false, name = "reference_count")
  private int referenceCount;

  @Column(nullable = false, name = "created_at")
  private Instant createdAt = Instant.now();

  @Version
  private long version;
}
