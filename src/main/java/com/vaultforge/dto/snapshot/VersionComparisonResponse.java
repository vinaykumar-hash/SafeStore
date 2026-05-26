package com.vaultforge.dto.snapshot;

import java.util.UUID;

public record VersionComparisonResponse(
    UUID versionA,
    UUID versionB,
    boolean sameChecksum,
    boolean sameLayout
) {}
