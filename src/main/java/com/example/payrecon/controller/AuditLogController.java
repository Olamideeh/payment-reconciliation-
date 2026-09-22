package com.example.payrecon.controller;

import com.example.payrecon.dto.AuditLogResponse;
import com.example.payrecon.service.AuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/audit-logs")
@RequiredArgsConstructor
public class AuditLogController {

    private final AuditService auditService;

    @GetMapping
    public ResponseEntity<List<AuditLogResponse>> getAuditLogs() {

        return ResponseEntity.ok(
                auditService.getAllAuditLogs()
        );
    }
}