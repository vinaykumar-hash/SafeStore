package com.vaultforge.repository;

import com.vaultforge.domain.RestoreJob;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RestoreJobRepository extends JpaRepository<RestoreJob, UUID> {}
