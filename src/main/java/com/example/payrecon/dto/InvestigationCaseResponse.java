package com.example.payrecon.dto;

import com.example.payrecon.enums.CaseStatus;
import com.example.payrecon.enums.ReconciliationStatus;

import java.time.LocalDateTime;

public record InvestigationCaseResponse(
        Long caseId,
        Long reconciliationResultId,
        String reference,
        ReconciliationStatus reconciliationStatus,
        CaseStatus caseStatus,
        String investigationNote,
        String investigatedBy,
        String proposedResolution,
        String submittedBy,
        String reviewedBy,
        String reviewComment,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}