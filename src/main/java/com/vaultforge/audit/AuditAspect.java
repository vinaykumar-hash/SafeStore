package com.vaultforge.audit;

import com.vaultforge.service.AuditService;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class AuditAspect {
  private final AuditService auditService;

  public AuditAspect(AuditService auditService) {
    this.auditService = auditService;
  }

  @AfterReturning("@annotation(audit)")
  public void afterAudit(JoinPoint joinPoint, Audit audit) {
    String principal = SecurityContextHolder.getContext().getAuthentication().getName();
    String entityId = resolveEntityId(joinPoint, audit);
    String ipAddress = resolveIpAddress();
    auditService.record(principal, audit.action(), audit.entityType(), entityId, ipAddress);
  }

  private String resolveEntityId(JoinPoint joinPoint, Audit audit) {
    int index = audit.entityIdArg();
    Object[] args = joinPoint.getArgs();
    if (index >= 0 && index < args.length && args[index] != null) {
      return String.valueOf(args[index]);
    }
    return joinPoint.getSignature().getName();
  }

  private String resolveIpAddress() {
    if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attrs) {
      return attrs.getRequest().getRemoteAddr();
    }
    return "unknown";
  }
}
