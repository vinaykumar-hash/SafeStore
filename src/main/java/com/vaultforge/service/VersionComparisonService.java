package com.vaultforge.service;

import com.vaultforge.domain.FileVersion;
import com.vaultforge.dto.snapshot.VersionComparisonResponse;
import com.vaultforge.exception.NotFoundException;
import com.vaultforge.repository.FileVersionRepository;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class VersionComparisonService {
  private final FileVersionRepository fileVersionRepository;

  public VersionComparisonService(FileVersionRepository fileVersionRepository) {
    this.fileVersionRepository = fileVersionRepository;
  }

  public VersionComparisonResponse compare(UUID versionA, UUID versionB) {
    FileVersion first = fileVersionRepository.findById(versionA)
        .orElseThrow(() -> new NotFoundException("Version A not found"));
    FileVersion second = fileVersionRepository.findById(versionB)
        .orElseThrow(() -> new NotFoundException("Version B not found"));
    boolean sameChecksum = first.getChecksum().equals(second.getChecksum());
    boolean sameLayout = String.valueOf(first.getSnapshotMetadata())
        .equals(String.valueOf(second.getSnapshotMetadata()));
    return new VersionComparisonResponse(first.getId(), second.getId(), sameChecksum, sameLayout);
  }
}
