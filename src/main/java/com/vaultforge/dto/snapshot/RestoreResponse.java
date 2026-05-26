package com.vaultforge.dto.snapshot;

import java.util.UUID;

public record RestoreResponse(UUID restoreJobId, UUID fileId, UUID fileVersionId, String status) {}
