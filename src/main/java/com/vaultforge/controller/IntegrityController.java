package com.vaultforge.controller;

import com.vaultforge.dto.integrity.IntegrityReportResponse;
import com.vaultforge.dto.integrity.IntegrityResponse;
import com.vaultforge.dto.integrity.IntegrityVerifyRequest;
import com.vaultforge.service.IntegrityService;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/integrity")
public class IntegrityController {
  private final IntegrityService integrityService;

  public IntegrityController(IntegrityService integrityService) {
    this.integrityService = integrityService;
  }

  @GetMapping("/{fileId}")
  public ResponseEntity<IntegrityResponse> validate(@PathVariable("fileId") UUID fileId) {
    return ResponseEntity.ok(integrityService.validate(fileId));
  }

  @GetMapping("/report/{fileId}")
  public ResponseEntity<IntegrityReportResponse> report(@PathVariable("fileId") UUID fileId) {
    return ResponseEntity.ok(integrityService.report(fileId));
  }

  @PostMapping("/verify")
  public ResponseEntity<IntegrityResponse> verify(@Valid @RequestBody IntegrityVerifyRequest request) {
    integrityService.assertIntegrity(request.fileId(), request.expectedChecksum());
    return ResponseEntity.ok(new IntegrityResponse(request.fileId(), true, request.expectedChecksum()));
  }
}
