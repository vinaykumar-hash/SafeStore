package com.vaultforge.dto.backup;

import java.util.List;

public record BackupDiffResult(
    List<Integer> changedOrders,
    int totalChunks,
    int changedChunks
) {}
