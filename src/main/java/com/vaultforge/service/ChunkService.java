package com.vaultforge.service;

import com.vaultforge.domain.Chunk;
import com.vaultforge.exception.NotFoundException;
import com.vaultforge.repository.ChunkRepository;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class ChunkService {
  private final ChunkRepository chunkRepository;

  public ChunkService(ChunkRepository chunkRepository) {
    this.chunkRepository = chunkRepository;
  }

  public Chunk getChunk(UUID chunkId) {
    return chunkRepository.findById(chunkId)
        .orElseThrow(() -> new NotFoundException("Chunk not found"));
  }

  public Chunk save(Chunk chunk) {
    return chunkRepository.save(chunk);
  }
}
