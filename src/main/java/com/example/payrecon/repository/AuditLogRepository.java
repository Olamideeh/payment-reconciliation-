package com.example.payrecon.repository;

import com.example.payrecon.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import com.example.payrecon.enums.AuditAction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface AuditLogRepository
        extends JpaRepository<AuditLog, Long> {

    List<AuditLog> findAllByOrderByCreatedAtDesc();

    List<AuditLog> findAllByEntityTypeAndEntityIdOrderByCreatedAtDesc(
            String entityType,
            Long entityId
    );
    Page<AuditLog> findAllByAction(
            AuditAction action,
            Pageable pageable
    );
}