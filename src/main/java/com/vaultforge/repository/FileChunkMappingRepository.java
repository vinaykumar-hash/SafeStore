package com.vaultforge.repository;

import com.vaultforge.domain.FileChunkMapping;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FileChunkMappingRepository extends JpaRepository<FileChunkMapping, UUID> {
  List<FileChunkMapping> findByFileIdOrderByChunkOrder(UUID fileId);

  Optional<FileChunkMapping> findByFileIdAndChunkOrder(UUID fileId, int chunkOrder);

  void deleteByFileId(UUID fileId);
}
