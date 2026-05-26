package com.vaultforge.repository;

import com.vaultforge.domain.Chunk;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChunkRepository extends JpaRepository<Chunk, UUID> {
  Optional<Chunk> findByHash(String hash);
}
