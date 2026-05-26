package com.vaultforge.service;

import com.vaultforge.exception.NotFoundException;
import com.vaultforge.repository.ChunkRepository;
import com.vaultforge.repository.FileChunkMappingRepository;
import com.vaultforge.storage.StorageService;
import java.io.InputStream;
import java.util.Comparator;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

@Service
public class ObjectReconstructionService {
  private final FileChunkMappingRepository mappingRepository;
  private final ChunkRepository chunkRepository;
  private final StorageService storageService;

  public ObjectReconstructionService(FileChunkMappingRepository mappingRepository,
                                     ChunkRepository chunkRepository,
                                     StorageService storageService) {
    this.mappingRepository = mappingRepository;
    this.chunkRepository = chunkRepository;
    this.storageService = storageService;
  }

  public StreamingResponseBody streamFile(UUID fileId) {
    return outputStream -> {
      mappingRepository.findByFileIdOrderByChunkOrder(fileId).stream()
          .sorted(Comparator.comparingInt(mapping -> mapping.getChunkOrder()))
          .forEach(mapping -> {
            var chunk = chunkRepository.findById(mapping.getChunkId())
                .orElseThrow(() -> new NotFoundException("Chunk not found"));
            try (InputStream input = storageService.getChunk(chunk.getStoragePath())) {
              input.transferTo(outputStream);
            } catch (Exception ex) {
              throw new IllegalStateException("Chunk reconstruction failed", ex);
            }
          });
    };
  }
}
