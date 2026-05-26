package com.vaultforge.dto.file;

import jakarta.validation.constraints.NotBlank;

public record UploadCompleteRequest(@NotBlank String checksum) {}
