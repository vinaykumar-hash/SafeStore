package com.vaultforge.dto.admin;

public record UploadMetricsResponse(
    long pending,
    long processing,
    long completed,
    long failed
) {}
