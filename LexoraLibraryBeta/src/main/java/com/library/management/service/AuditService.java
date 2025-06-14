package com.library.management.service;

import com.library.management.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AuditService {
    void logAction(String action, String entityType, Long entityId, String details);
    Page<AuditLog> getAuditLogsByEntityType(String entityType, Pageable pageable);
    Page<AuditLog> getAuditLogsByEntityId(Long entityId, Pageable pageable);
    Page<AuditLog> getAuditLogsByPerformedBy(String performedBy, Pageable pageable);
    Page<AuditLog> getAuditLogsByAction(String action, Pageable pageable);
    Page<AuditLog> getAllAuditLogs(Pageable pageable);
} 