package com.vaultforge.controller;

import com.vaultforge.dto.admin.AuditSearchRequest;
import com.vaultforge.dto.admin.UploadMetricsResponse;
import com.vaultforge.repository.AuditLogRepository;
import com.vaultforge.audit.AuditSpecifications;
import com.vaultforge.repository.UploadJobRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin")
public class AdminController {
  private final AuditLogRepository auditLogRepository;
  private final UploadJobRepository uploadJobRepository;

  public AdminController(AuditLogRepository auditLogRepository,
                         UploadJobRepository uploadJobRepository) {
    this.auditLogRepository = auditLogRepository;
    this.uploadJobRepository = uploadJobRepository;
  }

  @GetMapping("/audit")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<?> auditLogs() {
    return ResponseEntity.ok(auditLogRepository.findAll());
  }

  @PostMapping("/audit/search")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<Page<?>> auditSearch(@RequestBody AuditSearchRequest request,
                                             @RequestParam(value = "page", defaultValue = "0") int page,
                                             @RequestParam(value = "size", defaultValue = "50") int size,
                                             @RequestParam(value = "sortBy", defaultValue = "timestamp") String sortBy,
                                             @RequestParam(value = "sortDir", defaultValue = "desc") String sortDir) {
    Sort sort = sortDir.equalsIgnoreCase("asc")
        ? Sort.by(sortBy).ascending()
        : Sort.by(sortBy).descending();
    return ResponseEntity.ok(auditLogRepository.findAll(
        AuditSpecifications.from(request), PageRequest.of(page, size, sort)));
  }

  @GetMapping("/metrics/uploads")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<UploadMetricsResponse> uploadMetrics() {
    long pending = uploadJobRepository.countByStatus("PENDING");
    long processing = uploadJobRepository.countByStatus("PROCESSING");
    long completed = uploadJobRepository.countByStatus("COMPLETED");
    long failed = uploadJobRepository.countByStatus("FAILED");
    return ResponseEntity.ok(new UploadMetricsResponse(pending, processing, completed, failed));
  }
}
