package com.vaultforge.event;

import java.util.UUID;

public record UploadRequestedEvent(UUID uploadJobId, UUID fileId, UUID userId) {}
