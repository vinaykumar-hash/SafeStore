package com.vaultforge.repository;

import com.vaultforge.domain.FileVersion;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FileVersionRepository extends JpaRepository<FileVersion, UUID> {
  List<FileVersion> findByFileIdOrderByVersionNumberDesc(UUID fileId);

  void deleteByFileId(UUID fileId);
}
