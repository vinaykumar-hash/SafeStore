package com.vaultforge.dto.integrity;

public record CorruptChunkInfo(
    int chunkOrder,
    String expectedHash,
    String actualHash,
    String storagePath
) {}
