package com.example.payrecon.service;

import com.example.payrecon.entity.AuditLog;
import com.example.payrecon.enums.AuditAction;
import com.example.payrecon.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.example.payrecon.dto.AuditLogResponse;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import com.example.payrecon.enums.AuditAction;
@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    public void record(
            AuditAction action,
            String entityType,
            Long entityId,
            String performedBy,
            String details
    ) {

        AuditLog auditLog = new AuditLog();
        auditLog.setAction(action);
        auditLog.setEntityType(entityType);
        auditLog.setEntityId(entityId);
        auditLog.setPerformedBy(performedBy);
        auditLog.setDetails(details);

        auditLogRepository.save(auditLog);
    }@Transactional(readOnly = true)
    public Page<AuditLogResponse> getAllAuditLogs(
            AuditAction action,
            int page,
            int size
    ) {
        if (page < 0) {
            throw new IllegalArgumentException(
                    "Page number cannot be negative"
            );
        }

        if (size < 1 || size > 100) {
            throw new IllegalArgumentException(
                    "Page size must be between 1 and 100"
            );
        }

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(
                        Sort.Direction.DESC,
                        "createdAt"
                )
        );

        Page<AuditLog> auditLogs = action == null
                ? auditLogRepository.findAll(pageable)
                : auditLogRepository.findAllByAction(
                action,
                pageable
        );

        return auditLogs.map(auditLog ->
                new AuditLogResponse(
                        auditLog.getId(),
                        auditLog.getAction(),
                        auditLog.getEntityType(),
                        auditLog.getEntityId(),
                        auditLog.getPerformedBy(),
                        auditLog.getDetails(),
                        auditLog.getCreatedAt()
                )
        );
    }
}