package com.vaultforge.audit;

import com.vaultforge.domain.AuditLog;
import com.vaultforge.dto.admin.AuditSearchRequest;
import java.time.Instant;
import java.util.UUID;
import org.springframework.data.jpa.domain.Specification;

public final class AuditSpecifications {
  private AuditSpecifications() {}

  public static Specification<AuditLog> from(AuditSearchRequest request) {
    return Specification.where(userEquals(request.userId()))
        .and(actionEquals(request.action()))
        .and(entityTypeEquals(request.entityType()))
        .and(entityIdEquals(request.entityId()))
        .and(timestampAfter(request.from()))
        .and(timestampBefore(request.to()));
  }

  private static Specification<AuditLog> userEquals(String userId) {
    if (userId == null || userId.isBlank()) {
      return null;
    }
    return (root, query, builder) -> builder.equal(root.get("userId"), UUID.fromString(userId));
  }

  private static Specification<AuditLog> actionEquals(String action) {
    if (action == null || action.isBlank()) {
      return null;
    }
    return (root, query, builder) -> builder.equal(root.get("action"), action);
  }

  private static Specification<AuditLog> entityTypeEquals(String entityType) {
    if (entityType == null || entityType.isBlank()) {
      return null;
    }
    return (root, query, builder) -> builder.equal(root.get("entityType"), entityType);
  }

  private static Specification<AuditLog> entityIdEquals(String entityId) {
    if (entityId == null || entityId.isBlank()) {
      return null;
    }
    return (root, query, builder) -> builder.equal(root.get("entityId"), entityId);
  }

  private static Specification<AuditLog> timestampAfter(Instant after) {
    if (after == null) {
      return null;
    }
    return (root, query, builder) -> builder.greaterThanOrEqualTo(root.get("timestamp"), after);
  }

  private static Specification<AuditLog> timestampBefore(Instant before) {
    if (before == null) {
      return null;
    }
    return (root, query, builder) -> builder.lessThanOrEqualTo(root.get("timestamp"), before);
  }
}
