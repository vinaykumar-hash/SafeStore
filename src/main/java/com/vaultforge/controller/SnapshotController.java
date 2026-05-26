package com.vaultforge.controller;

import com.vaultforge.dto.snapshot.RestoreRequest;
import com.vaultforge.dto.snapshot.RestoreResponse;
import com.vaultforge.dto.snapshot.SnapshotCreateRequest;
import com.vaultforge.dto.snapshot.VersionComparisonResponse;
import com.vaultforge.service.RestoreService;
import com.vaultforge.service.SnapshotService;
import com.vaultforge.service.VersionComparisonService;
import com.vaultforge.exception.NotFoundException;
import com.vaultforge.repository.UserRepository;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/snapshots")
public class SnapshotController {
  private final SnapshotService snapshotService;
  private final RestoreService restoreService;
  private final VersionComparisonService comparisonService;
  private final UserRepository userRepository;

  public SnapshotController(SnapshotService snapshotService,
                            RestoreService restoreService,
                            VersionComparisonService comparisonService,
                            UserRepository userRepository) {
    this.snapshotService = snapshotService;
    this.restoreService = restoreService;
    this.comparisonService = comparisonService;
    this.userRepository = userRepository;
  }

  @PostMapping("/create")
  public ResponseEntity<Void> create(@Valid @RequestBody SnapshotCreateRequest request) {
    snapshotService.createSnapshot(request.fileId(), "PENDING");
    return ResponseEntity.accepted().build();
  }

  @PostMapping("/restore")
  public ResponseEntity<RestoreResponse> restore(@Valid @RequestBody RestoreRequest request,
                                                 Authentication authentication) {
    UUID userId = resolveUserId(authentication);
    var job = restoreService.restoreVersion(userId, request.fileVersionId());
    UUID fileId = restoreService.resolveFileId(request.fileVersionId());
    return ResponseEntity.accepted()
      .body(new RestoreResponse(job.getId(), fileId, request.fileVersionId(), job.getStatus()));
  }

  @GetMapping("/files/{fileId}")
  public ResponseEntity<?> list(@PathVariable("fileId") UUID fileId) {
    return ResponseEntity.ok(snapshotService.listVersions(fileId));
  }

  @GetMapping("/compare")
  public ResponseEntity<VersionComparisonResponse> compare(@RequestParam("versionA") UUID versionA,
                                                           @RequestParam("versionB") UUID versionB) {
    return ResponseEntity.ok(comparisonService.compare(versionA, versionB));
  }

  private UUID resolveUserId(Authentication authentication) {
    return userRepository.findByEmail(authentication.getName())
        .orElseThrow(() -> new NotFoundException("Authenticated user not found"))
        .getId();
  }
}
