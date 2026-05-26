package com.vaultforge.dto.integrity;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record IntegrityVerifyRequest(
    @NotNull UUID fileId,
    @NotBlank String expectedChecksum
) {}
