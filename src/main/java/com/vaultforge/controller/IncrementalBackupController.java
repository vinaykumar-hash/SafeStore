package com.vaultforge.controller;

import com.vaultforge.dto.backup.BackupDiffResult;
import com.vaultforge.dto.backup.IncrementalPlanRequest;
import com.vaultforge.dto.backup.IncrementalPlanResponse;
import com.vaultforge.service.IncrementalBackupService;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/backups")
public class IncrementalBackupController {
  private final IncrementalBackupService incrementalBackupService;

  public IncrementalBackupController(IncrementalBackupService incrementalBackupService) {
    this.incrementalBackupService = incrementalBackupService;
  }

  @PostMapping("/incremental/plan")
  public ResponseEntity<IncrementalPlanResponse> plan(@Valid @RequestBody IncrementalPlanRequest request) {
    return ResponseEntity.ok(incrementalBackupService.planIncremental(request.fileId(), request.chunkHashes()));
  }

  @GetMapping("/incremental/diff")
  public ResponseEntity<BackupDiffResult> diff(@RequestParam("versionA") UUID versionA,
                                               @RequestParam("versionB") UUID versionB) {
    return ResponseEntity.ok(incrementalBackupService.diffVersions(versionA, versionB));
  }
}
