package com.example.payrecon.controller;

import com.example.payrecon.dto.AuditLogResponse;
import com.example.payrecon.service.AuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.payrecon.enums.AuditAction;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/audit-logs")
@RequiredArgsConstructor
@Tag(
        name = "Audit Logs",
        description = "Admin-only history of important PayRecon actions"
)
public class AuditLogController {

    private final AuditService auditService;
    @Operation(
            summary = "View audit logs",
            description = "Admin can filter and paginate recorded system actions"
    )
    @GetMapping
    public ResponseEntity<Page<AuditLogResponse>> getAuditLogs(
            @RequestParam(required = false)
            AuditAction action,

            @RequestParam(defaultValue = "0")
            int page,

            @RequestParam(defaultValue = "20")
            int size
    ) {

        return ResponseEntity.ok(
                auditService.getAllAuditLogs(
                        action,
                        page,
                        size
                )
        );
    }
}