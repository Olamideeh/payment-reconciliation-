package com.example.payrecon.service;

import com.example.payrecon.entity.AuditLog;
import com.example.payrecon.enums.AuditAction;
import com.example.payrecon.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.example.payrecon.dto.AuditLogResponse;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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
    }
    @Transactional(readOnly = true)
    public List<AuditLogResponse> getAllAuditLogs() {

        return auditLogRepository
                .findAllByOrderByCreatedAtDesc()
                .stream()
                .map(auditLog -> new AuditLogResponse(
                        auditLog.getId(),
                        auditLog.getAction(),
                        auditLog.getEntityType(),
                        auditLog.getEntityId(),
                        auditLog.getPerformedBy(),
                        auditLog.getDetails(),
                        auditLog.getCreatedAt()
                ))
                .toList();
    }
}