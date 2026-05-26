package com.vaultforge.repository;

import com.vaultforge.domain.UploadJob;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UploadJobRepository extends JpaRepository<UploadJob, UUID> {
	Optional<UploadJob> findFirstByFileIdOrderByCreatedAtDesc(UUID fileId);

	long countByStatus(String status);

	void deleteByFileId(UUID fileId);
}

