package com.library.management.service;

import com.library.management.entity.AuditLog;
import com.library.management.repository.AuditLogRepository;
import com.library.management.service.impl.AuditServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = org.mockito.quality.Strictness.LENIENT)
class AuditServiceTest {

    @Mock
    private AuditLogRepository auditLogRepository;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    private AuditService auditService;

    private AuditLog testAuditLog;
    private Pageable pageable;

    @BeforeEach
    void setUp() {
        auditService = new AuditServiceImpl(auditLogRepository);
        testAuditLog = AuditLog.builder()
            .entityType("Book")
            .entityId(1L)
            .action("CREATE")
            .performedBy("testUser")
            .details("Created new book")
            .createdAt(LocalDateTime.now())
            .build();

        pageable = PageRequest.of(0, 10);

        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("testUser");
    }

    @Test
    void logAction_Success() {
        String action = "CREATE";
        String entityType = "BOOK";
        Long entityId = 1L;
        String details = "Created new book";

        auditService.logAction(action, entityType, entityId, details);

        ArgumentCaptor<AuditLog> auditLogCaptor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(auditLogCaptor.capture());

        AuditLog savedAuditLog = auditLogCaptor.getValue();
        assertEquals(action, savedAuditLog.getAction());
        assertEquals(entityType, savedAuditLog.getEntityType());
        assertEquals(entityId, savedAuditLog.getEntityId());
        assertEquals(details, savedAuditLog.getDetails());
        assertEquals("testUser", savedAuditLog.getPerformedBy());
    }

    @Test
    void logAction_NoAuthentication() {
        when(securityContext.getAuthentication()).thenReturn(null);

        auditService.logAction("CREATE", "BOOK", 1L, "Created new book");

        verify(auditLogRepository).save(any(AuditLog.class));
    }

    @Test
    void logAction_NullUsername() {
        when(authentication.getName()).thenReturn(null);

        auditService.logAction("CREATE", "BOOK", 1L, "Created new book");

        verify(auditLogRepository).save(any(AuditLog.class));
    }

    @Test
    void logAction_EmptyUsername() {
        when(authentication.getName()).thenReturn("");

        auditService.logAction("CREATE", "BOOK", 1L, "Created new book");

        verify(auditLogRepository).save(any(AuditLog.class));
    }

    @Test
    void getAuditLogsByEntityType_Success() {
        Page<AuditLog> auditLogPage = new PageImpl<>(List.of(testAuditLog));
        when(auditLogRepository.findByEntityTypeOrderByCreatedAtDesc("Book", pageable)).thenReturn(auditLogPage);

        Page<AuditLog> result = auditService.getAuditLogsByEntityType("Book", pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        AuditLog firstLog = result.getContent().get(0);
        assertEquals("Book", firstLog.getEntityType());
        assertEquals(1L, firstLog.getEntityId());
        assertEquals("CREATE", firstLog.getAction());
        assertEquals("testUser", firstLog.getPerformedBy());
        verify(auditLogRepository).findByEntityTypeOrderByCreatedAtDesc("Book", pageable);
    }

    @Test
    void getAuditLogsByPerformedBy_Success() {
        Page<AuditLog> auditLogPage = new PageImpl<>(List.of(testAuditLog));
        when(auditLogRepository.findByPerformedByOrderByCreatedAtDesc("testUser", pageable)).thenReturn(auditLogPage);

        Page<AuditLog> result = auditService.getAuditLogsByPerformedBy("testUser", pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        AuditLog firstLog = result.getContent().get(0);
        assertEquals("Book", firstLog.getEntityType());
        assertEquals(1L, firstLog.getEntityId());
        assertEquals("CREATE", firstLog.getAction());
        assertEquals("testUser", firstLog.getPerformedBy());
        verify(auditLogRepository).findByPerformedByOrderByCreatedAtDesc("testUser", pageable);
    }

    @Test
    void getAuditLogsByAction_Success() {
        Page<AuditLog> auditLogPage = new PageImpl<>(List.of(testAuditLog));
        when(auditLogRepository.findByActionOrderByCreatedAtDesc("CREATE", pageable)).thenReturn(auditLogPage);

        Page<AuditLog> result = auditService.getAuditLogsByAction("CREATE", pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        AuditLog firstLog = result.getContent().get(0);
        assertEquals("Book", firstLog.getEntityType());
        assertEquals(1L, firstLog.getEntityId());
        assertEquals("CREATE", firstLog.getAction());
        assertEquals("testUser", firstLog.getPerformedBy());
        verify(auditLogRepository).findByActionOrderByCreatedAtDesc("CREATE", pageable);
    }
} 