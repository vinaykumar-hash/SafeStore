package com.vaultforge.service;

import com.vaultforge.backup.BackupOptimizer;
import com.vaultforge.backup.ChunkDiffEngine;
import com.vaultforge.dto.backup.BackupDiffResult;
import com.vaultforge.dto.backup.IncrementalPlanResponse;
import com.vaultforge.exception.NotFoundException;
import com.vaultforge.repository.FileVersionRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class IncrementalBackupService {
  private final FileVersionRepository fileVersionRepository;
  private final ChunkDiffEngine diffEngine;
  private final BackupOptimizer backupOptimizer;

  public IncrementalBackupService(FileVersionRepository fileVersionRepository,
                                  ChunkDiffEngine diffEngine,
                                  BackupOptimizer backupOptimizer) {
    this.fileVersionRepository = fileVersionRepository;
    this.diffEngine = diffEngine;
    this.backupOptimizer = backupOptimizer;
  }

  public IncrementalPlanResponse planIncremental(UUID fileId, List<String> chunkHashes) {
    String snapshotMetadata = fileVersionRepository.findByFileIdOrderByVersionNumberDesc(fileId).stream()
        .findFirst()
        .map(version -> version.getSnapshotMetadata())
        .orElse("");
    BackupDiffResult diff = diffEngine.diffAgainstSnapshot(chunkHashes, snapshotMetadata);
    var metrics = backupOptimizer.buildMetrics(diff.totalChunks(), diff.changedChunks());
    return new IncrementalPlanResponse(fileId, diff.changedOrders(), metrics);
  }

  public BackupDiffResult diffVersions(UUID versionA, UUID versionB) {
    var first = fileVersionRepository.findById(versionA)
        .orElseThrow(() -> new NotFoundException("Version A not found"));
    var second = fileVersionRepository.findById(versionB)
        .orElseThrow(() -> new NotFoundException("Version B not found"));
    return diffEngine.diffSnapshots(first.getSnapshotMetadata(), second.getSnapshotMetadata());
  }
}
