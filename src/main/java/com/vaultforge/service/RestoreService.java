package com.vaultforge.service;

import com.vaultforge.audit.Audit;
import com.vaultforge.domain.RestoreJob;
import com.vaultforge.domain.FileMetadata;
import com.vaultforge.domain.FileVersion;
import com.vaultforge.domain.Chunk;
import com.vaultforge.exception.NotFoundException;
import com.vaultforge.repository.ChunkRepository;
import com.vaultforge.repository.FileChunkMappingRepository;
import com.vaultforge.repository.FileMetadataRepository;
import com.vaultforge.repository.FileVersionRepository;
import com.vaultforge.repository.RestoreJobRepository;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RestoreService {
  private final RestoreJobRepository restoreJobRepository;
  private final FileVersionRepository fileVersionRepository;
  private final FileMetadataRepository fileMetadataRepository;
  private final FileChunkMappingRepository mappingRepository;
  private final ChunkRepository chunkRepository;
  private final SnapshotService snapshotService;
  private final IntegrityService integrityService;
  private final ConcurrencyService concurrencyService;

  public RestoreService(RestoreJobRepository restoreJobRepository,
                        FileVersionRepository fileVersionRepository,
                        FileMetadataRepository fileMetadataRepository,
                        FileChunkMappingRepository mappingRepository,
                        ChunkRepository chunkRepository,
                        SnapshotService snapshotService,
                        IntegrityService integrityService,
                        ConcurrencyService concurrencyService) {
    this.restoreJobRepository = restoreJobRepository;
    this.fileVersionRepository = fileVersionRepository;
    this.fileMetadataRepository = fileMetadataRepository;
    this.mappingRepository = mappingRepository;
    this.chunkRepository = chunkRepository;
    this.snapshotService = snapshotService;
    this.integrityService = integrityService;
    this.concurrencyService = concurrencyService;
  }

  @Audit(action = "RESTORE_START", entityType = "RESTORE_JOB", entityIdArg = 0)
  public RestoreJob startRestore(UUID userId) {
    RestoreJob job = new RestoreJob();
    job.setUserId(userId);
    job.setStatus("PENDING");
    return restoreJobRepository.save(job);
  }

  @Audit(action = "RESTORE_VERSION", entityType = "FILE_VERSION", entityIdArg = 1)
  public RestoreJob restoreVersion(UUID userId, UUID fileVersionId) {
    return concurrencyService.executeWithRetry(() -> {
      RestoreJob job = startRestore(userId);
      FileVersion version = fileVersionRepository.findById(fileVersionId)
          .orElseThrow(() -> new NotFoundException("Version not found"));
      FileMetadata metadata = fileMetadataRepository.findById(version.getFileId())
          .orElseThrow(() -> new NotFoundException("File not found"));

      mappingRepository.deleteByFileId(metadata.getId());
      applySnapshotMetadata(metadata.getId(), version.getSnapshotMetadata());
      metadata.setChecksum(version.getChecksum());
      fileMetadataRepository.save(metadata);
      integrityService.assertIntegrity(metadata.getId(), version.getChecksum());
      snapshotService.createSnapshot(metadata.getId(), version.getChecksum());

      job.setStatus("COMPLETED");
      return restoreJobRepository.save(job);
    });
  }

  public UUID resolveFileId(UUID fileVersionId) {
    return fileVersionRepository.findById(fileVersionId)
        .map(FileVersion::getFileId)
        .orElseThrow(() -> new NotFoundException("Version not found"));
  }

  private void applySnapshotMetadata(UUID fileId, String snapshotMetadata) {
    if (snapshotMetadata == null || snapshotMetadata.isBlank()) {
      return;
    }
    String[] entries = snapshotMetadata.split(",");
    for (String entry : entries) {
      String[] parts = entry.split(":", 2);
      if (parts.length != 2) {
        continue;
      }
      int order = Integer.parseInt(parts[0]);
      String hash = parts[1];
      Chunk chunk = chunkRepository.findByHash(hash)
          .orElseThrow(() -> new NotFoundException("Chunk not found for restore"));
      com.vaultforge.domain.FileChunkMapping mapping = new com.vaultforge.domain.FileChunkMapping();
      mapping.setFileId(fileId);
      mapping.setChunkId(chunk.getId());
      mapping.setChunkOrder(order);
      mappingRepository.save(mapping);
    }
  }
}
