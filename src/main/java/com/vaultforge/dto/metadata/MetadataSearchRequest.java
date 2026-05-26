package com.vaultforge.dto.metadata;

import java.time.Instant;

public record MetadataSearchRequest(
	String filename,
	String ownerId,
	String tags,
	Instant uploadedAfter,
	Instant uploadedBefore
) {}
