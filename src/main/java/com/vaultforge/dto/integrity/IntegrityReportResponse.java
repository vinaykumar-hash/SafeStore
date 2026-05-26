package com.vaultforge.dto.integrity;

import java.util.List;
import java.util.UUID;

public record IntegrityReportResponse(
    UUID fileId,
    boolean valid,
    String checksum,
    List<CorruptChunkInfo> corruptChunks
) {}
