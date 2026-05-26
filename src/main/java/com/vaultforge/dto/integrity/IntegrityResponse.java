package com.vaultforge.dto.integrity;

import java.util.UUID;

public record IntegrityResponse(UUID fileId, boolean valid, String checksum) {}
