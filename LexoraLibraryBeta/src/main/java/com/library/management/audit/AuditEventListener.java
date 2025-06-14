package com.library.management.audit;

import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.Optional;

public class AuditEventListener {

    private static final Logger logger = LoggerFactory.getLogger(AuditEventListener.class);

    @PrePersist
    public void onPrePersist(Object entity) {
        String user = getCurrentUser();
        logger.info("Creating new entity: {} by user: {}", entity.getClass().getSimpleName(), user);
    }

    @PreUpdate
    public void onPreUpdate(Object entity) {
        String user = getCurrentUser();
        logger.info("Updating entity: {} by user: {}", entity.getClass().getSimpleName(), user);
    }

    private String getCurrentUser() {
        return Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
                .map(Authentication::getName)
                .orElse("system");
    }
} 