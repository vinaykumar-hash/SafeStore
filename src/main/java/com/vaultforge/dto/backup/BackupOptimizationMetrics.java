package com.vaultforge.dto.backup;

public record BackupOptimizationMetrics(
    int totalChunks,
    int changedChunks,
    int reusedChunks,
    double changeRatio
) {}
