package com.vaultforge.event;

import java.util.UUID;

public record ChunkUploadEvent(
	UUID uploadJobId,
	UUID fileId,
	int chunkOrder,
	String chunkHash,
	String storagePath,
	long sizeBytes,
	String dataBase64
) {}
