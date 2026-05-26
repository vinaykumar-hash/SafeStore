package com.vaultforge.dto.admin;

import java.time.Instant;

public record AuditSearchRequest(
	String userId,
	String action,
	String entityType,
	String entityId,
	Instant from,
	Instant to
) {}
