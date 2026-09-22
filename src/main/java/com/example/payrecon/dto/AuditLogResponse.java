package com.example.payrecon.dto;

import com.example.payrecon.enums.AuditAction;

import java.time.LocalDateTime;

public record AuditLogResponse(
        Long id,
        AuditAction action,
        String entityType,
        Long entityId,
        String performedBy,
        String details,
        LocalDateTime createdAt
) {
}