package com.vaultforge.dto.backup;

import java.util.List;
import java.util.UUID;

public record IncrementalPlanResponse(
    UUID fileId,
    List<Integer> changedOrders,
    BackupOptimizationMetrics metrics
) {}
