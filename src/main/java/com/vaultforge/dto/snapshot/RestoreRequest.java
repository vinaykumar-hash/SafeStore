package com.vaultforge.dto.snapshot;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record RestoreRequest(@NotNull UUID fileVersionId) {}
