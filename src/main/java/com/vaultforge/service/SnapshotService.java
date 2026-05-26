package com.vaultforge.service;

import com.vaultforge.audit.Audit;
import com.vaultforge.domain.FileVersion;
import com.vaultforge.exception.NotFoundException;
import com.vaultforge.repository.ChunkRepository;
import com.vaultforge.repository.FileChunkMappingRepository;
import com.vaultforge.repository.FileVersionRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SnapshotService {
  private final FileVersionRepository fileVersionRepository;
  private final FileChunkMappingRepository mappingRepository;
  private final ChunkRepository chunkRepository;
  private final ConcurrencyService concurrencyService;

  public SnapshotService(FileVersionRepository fileVersionRepository,
                         FileChunkMappingRepository mappingRepository,
                         ChunkRepository chunkRepository,
                         ConcurrencyService concurrencyService) {
    this.fileVersionRepository = fileVersionRepository;
    this.mappingRepository = mappingRepository;
    this.chunkRepository = chunkRepository;
    this.concurrencyService = concurrencyService;
  }

  @Audit(action = "SNAPSHOT_CREATE", entityType = "FILE_VERSION", entityIdArg = 0)
  @CacheEvict(cacheNames = "snapshot-summaries", key = "#p0")
  public FileVersion createSnapshot(UUID fileId, String checksum) {
    return concurrencyService.executeWithRetry(() -> {
      List<FileVersion> versions = fileVersionRepository.findByFileIdOrderByVersionNumberDesc(fileId);
      int nextVersion = versions.isEmpty() ? 1 : versions.get(0).getVersionNumber() + 1;
      FileVersion version = new FileVersion();
      version.setFileId(fileId);
      version.setVersionNumber(nextVersion);
      version.setChecksum(checksum);
      version.setSnapshotMetadata(buildSnapshotMetadata(fileId));
      return fileVersionRepository.save(version);
    });
  }

  @Cacheable(cacheNames = "snapshot-summaries", key = "#p0")
  public List<FileVersion> listVersions(UUID fileId) {
    return fileVersionRepository.findByFileIdOrderByVersionNumberDesc(fileId);
  }

  private String buildSnapshotMetadata(UUID fileId) {
    StringBuilder builder = new StringBuilder();
    mappingRepository.findByFileIdOrderByChunkOrder(fileId).forEach(mapping -> {
      String hash = chunkRepository.findById(mapping.getChunkId())
          .map(chunk -> chunk.getHash())
          .orElseThrow(() -> new NotFoundException("Chunk missing for snapshot"));
      if (!builder.isEmpty()) {
        builder.append(',');
      }
      builder.append(mapping.getChunkOrder()).append(':').append(hash);
    });
    return builder.toString();
  }
}
