package com.vaultforge.dto.metadata;

import java.time.Instant;
import java.util.UUID;

public record MetadataResponse(
    UUID id,
    String filename,
    UUID ownerId,
    long totalSize,
    String checksum,
    Instant uploadTimestamp,
    String tags
) {}
