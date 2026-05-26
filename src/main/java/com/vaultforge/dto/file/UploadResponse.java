package com.vaultforge.dto.file;

import java.util.UUID;

public record UploadResponse(UUID uploadJobId, UUID fileId, String status) {}
