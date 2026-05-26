package com.vaultforge.dto.backup;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

public record IncrementalPlanRequest(
    @NotNull UUID fileId,
    @NotEmpty List<String> chunkHashes
) {}
