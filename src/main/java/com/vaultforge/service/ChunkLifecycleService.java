package com.vaultforge.service;

import com.vaultforge.domain.Chunk;
import com.vaultforge.exception.NotFoundException;
import com.vaultforge.repository.ChunkRepository;
import com.vaultforge.storage.StorageService;
import org.springframework.stereotype.Service;
import com.vaultforge.audit.Audit;

@Service
public class ChunkLifecycleService {
  private final ChunkRepository chunkRepository;
  private final StorageService storageService;

  public ChunkLifecycleService(ChunkRepository chunkRepository, StorageService storageService) {
    this.chunkRepository = chunkRepository;
    this.storageService = storageService;
  }

  @Audit(action = "CHUNK_DELETE", entityType = "CHUNK", entityIdArg = 0)
  public void deleteByHash(String hash) {
    Chunk chunk = chunkRepository.findByHash(hash)
        .orElseThrow(() -> new NotFoundException("Chunk not found"));
    int references = chunk.getReferenceCount();
    if (references > 1) {
      chunk.setReferenceCount(references - 1);
      chunkRepository.save(chunk);
      return;
    }
    storageService.deleteChunk(chunk.getStoragePath());
    chunkRepository.delete(chunk);
  }
}
