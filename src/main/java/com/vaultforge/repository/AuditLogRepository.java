package com.vaultforge.repository;

import com.vaultforge.domain.AuditLog;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface AuditLogRepository extends JpaRepository<AuditLog, UUID>,
	JpaSpecificationExecutor<AuditLog> {}
