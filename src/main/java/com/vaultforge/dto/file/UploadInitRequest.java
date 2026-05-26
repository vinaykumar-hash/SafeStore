package com.vaultforge.dto.file;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UploadInitRequest(
    @NotBlank String filename,
    @NotNull Long totalSize,
    String tags
) {}
