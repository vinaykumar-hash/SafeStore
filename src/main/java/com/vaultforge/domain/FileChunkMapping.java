package com.vaultforge.domain;

import jakarta.persistence.*;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(
  name = "file_chunk_mapping",
  indexes = {
    @Index(name = "idx_file_chunk_file", columnList = "file_id"),
    @Index(name = "idx_file_chunk_chunk", columnList = "chunk_id"),
    @Index(name = "idx_file_chunk_order", columnList = "file_id,chunk_order", unique = true)
  }
)
@Getter
@Setter
public class FileChunkMapping {
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
      foreignKey = @ForeignKey(name = "fk_file_chunk_mapping_file")
  )
  private FileMetadata file;

  @Column(nullable = false, name = "chunk_id")
  private UUID chunkId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(
      name = "chunk_id",
      referencedColumnName = "id",
      insertable = false,
      updatable = false,
      foreignKey = @ForeignKey(name = "fk_file_chunk_mapping_chunk")
  )
  private Chunk chunk;

  @Column(nullable = false, name = "chunk_order")
  private int chunkOrder;

  @Version
  private long version;
}
