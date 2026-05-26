package com.vaultforge.repository;

import com.vaultforge.domain.FileMetadata;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface FileMetadataRepository extends JpaRepository<FileMetadata, UUID>,
	JpaSpecificationExecutor<FileMetadata> {}
