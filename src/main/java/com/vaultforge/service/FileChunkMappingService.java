package com.vaultforge.service;

import com.vaultforge.domain.FileChunkMapping;
import com.vaultforge.repository.FileChunkMappingRepository;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class FileChunkMappingService {
  private final FileChunkMappingRepository mappingRepository;
  private final ConcurrencyService concurrencyService;

  public FileChunkMappingService(FileChunkMappingRepository mappingRepository,
                                 ConcurrencyService concurrencyService) {
    this.mappingRepository = mappingRepository;
    this.concurrencyService = concurrencyService;
  }

  public FileChunkMapping addMapping(UUID fileId, UUID chunkId, int order) {
    return concurrencyService.executeWithRetry(() ->
        mappingRepository.findByFileIdAndChunkOrder(fileId, order)
            .map(existing -> {
              if (existing.getChunkId().equals(chunkId)) {
                return existing;
              }
              existing.setChunkId(chunkId);
              return mappingRepository.save(existing);
            })
            .orElseGet(() -> {
              FileChunkMapping mapping = new FileChunkMapping();
              mapping.setFileId(fileId);
              mapping.setChunkId(chunkId);
              mapping.setChunkOrder(order);
              return mappingRepository.save(mapping);
            }));
  }
}
