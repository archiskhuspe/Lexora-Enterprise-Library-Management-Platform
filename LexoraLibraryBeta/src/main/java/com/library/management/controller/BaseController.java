package com.library.management.controller;

import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

public abstract class BaseController {
    
    protected <T> ResponseEntity<Page<T>> createPaginatedResponse(Page<T> page) {
        return ResponseEntity.ok(page);
    }

    protected <T> ResponseEntity<T> ok(T body) {
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(body);
    }

    protected <T> ResponseEntity<T> created(T body) {
        return ResponseEntity.created(null)
                .contentType(MediaType.APPLICATION_JSON)
                .body(body);
    }
} 