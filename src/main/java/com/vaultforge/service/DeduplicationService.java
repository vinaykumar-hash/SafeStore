package com.vaultforge.service;

import com.vaultforge.domain.Chunk;
import com.vaultforge.repository.ChunkRepository;
import java.util.Optional;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
public class DeduplicationService {
  private final ChunkRepository chunkRepository;
  private final ConcurrencyService concurrencyService;

  public DeduplicationService(ChunkRepository chunkRepository,
                              ConcurrencyService concurrencyService) {
    this.chunkRepository = chunkRepository;
    this.concurrencyService = concurrencyService;
  }

  public Chunk ensureChunk(String hash, String storagePath, long size) {
    return concurrencyService.executeWithRetry(() ->
        chunkRepository.findByHash(hash).map(existing -> {
          existing.setReferenceCount(existing.getReferenceCount() + 1);
          return chunkRepository.save(existing);
        }).orElseGet(() -> {
          Chunk chunk = new Chunk();
          chunk.setHash(hash);
          chunk.setStoragePath(storagePath);
          chunk.setSize(size);
          chunk.setReferenceCount(1);
          return chunkRepository.save(chunk);
        }));
  }

  public Optional<Chunk> findByHash(String hash) {
    return chunkRepository.findByHash(hash);
  }

  @Cacheable(cacheNames = "chunk-exists", key = "#p0")
  public boolean chunkExists(String hash) {
    return chunkRepository.findByHash(hash).isPresent();
  }
}
