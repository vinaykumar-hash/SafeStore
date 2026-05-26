package com.vaultforge.controller;

import com.vaultforge.service.ChunkLifecycleService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/chunks")
public class ChunkController {
  private final ChunkLifecycleService lifecycleService;

  public ChunkController(ChunkLifecycleService lifecycleService) {
    this.lifecycleService = lifecycleService;
  }

  @DeleteMapping("/{hash}")
  @PreAuthorize("hasRole('STORAGE_MANAGER') or hasRole('ADMIN')")
  public ResponseEntity<Void> delete(@PathVariable("hash") String hash) {
    lifecycleService.deleteByHash(hash);
    return ResponseEntity.noContent().build();
  }
}
