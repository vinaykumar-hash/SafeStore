package com.vaultforge.dto.file;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ChunkUploadRequest(
    @NotNull Integer chunkOrder,
    @NotBlank String chunkHash,
    @NotBlank String storagePath,
    @NotNull Long sizeBytes,
    @NotBlank String dataBase64
) {}
