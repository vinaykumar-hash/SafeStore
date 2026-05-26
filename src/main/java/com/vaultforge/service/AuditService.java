package com.vaultforge.service;

import com.vaultforge.domain.AuditLog;
import com.vaultforge.repository.AuditLogRepository;
import com.vaultforge.repository.UserRepository;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class AuditService {
  private final AuditLogRepository auditLogRepository;
  private final UserRepository userRepository;

  public AuditService(AuditLogRepository auditLogRepository, UserRepository userRepository) {
    this.auditLogRepository = auditLogRepository;
    this.userRepository = userRepository;
  }

  public void record(String email, String action, String entityType, String entityId, String ipAddress) {
    UUID userId = userRepository.findByEmail(email)
        .map(user -> user.getId())
        .orElseThrow(() -> new IllegalStateException("Authenticated user not found"));
    recordUser(userId, action, entityType, entityId, ipAddress);
  }

  public void recordUser(UUID userId, String action, String entityType, String entityId, String ipAddress) {
    AuditLog log = new AuditLog();
    log.setUserId(userId);
    log.setAction(action);
    log.setEntityType(entityType);
    log.setEntityId(entityId);
    log.setIpAddress(ipAddress);
    auditLogRepository.save(log);
  }
}
