package com.example.payrecon.service;

import com.example.payrecon.dto.InvestigationCaseResponse;
import com.example.payrecon.entity.InvestigationCase;
import com.example.payrecon.entity.ReconciliationResult;
import com.example.payrecon.enums.CaseStatus;
import com.example.payrecon.exception.ResourceNotFoundException;
import com.example.payrecon.repository.InvestigationCaseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.payrecon.dto.StartInvestigationRequest;
import com.example.payrecon.dto.SubmitResolutionRequest;
import com.example.payrecon.dto.ReviewResolutionRequest;
import com.example.payrecon.enums.AuditAction;
@Service
@RequiredArgsConstructor
public class InvestigationCaseService {

    private final InvestigationCaseRepository caseRepository;
    private final AuditService auditService;

    @Transactional(readOnly = true)
    public Page<InvestigationCaseResponse> getCases(
            CaseStatus status,
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

        Page<InvestigationCase> cases = status == null
                ? caseRepository.findAll(pageable)
                : caseRepository.findAllByStatus(
                status,
                pageable
        );

        return cases.map(this::mapToResponse);
    }

    private InvestigationCaseResponse mapToResponse(
            InvestigationCase investigationCase
    ) {

        ReconciliationResult result =
                investigationCase.getReconciliationResult();

        return new InvestigationCaseResponse(
                investigationCase.getId(),
                result.getId(),
                result.getReference(),
                result.getStatus(),
                investigationCase.getStatus(),
                investigationCase.getInvestigationNote(),
                investigationCase.getInvestigatedBy(),
                investigationCase.getProposedResolution(),
                investigationCase.getSubmittedBy(),
                investigationCase.getReviewedBy(),
                investigationCase.getReviewComment(),
                investigationCase.getCreatedAt(),
                investigationCase.getUpdatedAt()
        );
    }
    @Transactional
    public InvestigationCaseResponse startInvestigation(
            Long caseId,
            String authenticatedOfficer,
            StartInvestigationRequest request
    ) {

        InvestigationCase investigationCase =
                caseRepository.findById(caseId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Investigation case not found"
                                )
                        );

        if (investigationCase.getStatus() != CaseStatus.OPEN) {
            throw new IllegalStateException(
                    "Only an OPEN case can be investigated"
            );
        }

        investigationCase.setStatus(CaseStatus.UNDER_REVIEW);
        investigationCase.setInvestigatedBy(authenticatedOfficer);
        investigationCase.setInvestigationNote(
                request.getInvestigationNote()
        );

        InvestigationCase savedCase =
                caseRepository.save(investigationCase);
        auditService.record(
                AuditAction.INVESTIGATION_STARTED,
                "INVESTIGATION_CASE",
                savedCase.getId(),
                authenticatedOfficer,
                "Investigation started"
        );

        return mapToResponse(savedCase);


    }
    @Transactional
    public InvestigationCaseResponse submitResolution(
            Long caseId,
            String authenticatedOfficer,
            SubmitResolutionRequest request
    ) {

        InvestigationCase investigationCase =
                caseRepository.findById(caseId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Investigation case not found"
                                )
                        );

        if (investigationCase.getStatus()
                != CaseStatus.UNDER_REVIEW) {

            throw new IllegalStateException(
                    "Only a case under review can be submitted"
            );
        }

        investigationCase.setProposedResolution(
                request.proposedResolution()
        );

        investigationCase.setSubmittedBy(authenticatedOfficer);
        investigationCase.setStatus(CaseStatus.PENDING_APPROVAL);

        InvestigationCase savedCase =
                caseRepository.save(investigationCase);
        auditService.record(
                AuditAction.RESOLUTION_SUBMITTED,
                "INVESTIGATION_CASE",
                savedCase.getId(),
                authenticatedOfficer,
                "Resolution submitted for Admin approval"
        );

        return mapToResponse(savedCase);
    }
    @Transactional
    public InvestigationCaseResponse approveResolution(
            Long caseId,
            String authenticatedAdmin,
            ReviewResolutionRequest request
    ) {

        return reviewResolution(
                caseId,
                authenticatedAdmin,
                request,
                CaseStatus.APPROVED
        );
    }

    @Transactional
    public InvestigationCaseResponse rejectResolution(
            Long caseId,
            String authenticatedAdmin,
            ReviewResolutionRequest request
    ) {

        return reviewResolution(
                caseId,
                authenticatedAdmin,
                request,
                CaseStatus.REJECTED
        );
    }

    private InvestigationCaseResponse reviewResolution(
            Long caseId,
            String authenticatedAdmin,
            ReviewResolutionRequest request,
            CaseStatus decision
    ) {

        InvestigationCase investigationCase =
                caseRepository.findById(caseId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Investigation case not found"
                                )
                        );

        if (investigationCase.getStatus()
                != CaseStatus.PENDING_APPROVAL) {

            throw new IllegalStateException(
                    "Only a pending case can be reviewed"
            );
        }

        if (authenticatedAdmin.equalsIgnoreCase(
                investigationCase.getSubmittedBy()
        )) {
            throw new IllegalStateException(
                    "The maker cannot approve or reject their own resolution"
            );
        }

        investigationCase.setStatus(decision);
        investigationCase.setReviewedBy(authenticatedAdmin);
        investigationCase.setReviewComment(
                request.reviewComment()
        );

        InvestigationCase savedCase =
                caseRepository.save(investigationCase);
        AuditAction auditAction =
                decision == CaseStatus.APPROVED
                        ? AuditAction.RESOLUTION_APPROVED
                        : AuditAction.RESOLUTION_REJECTED;

        auditService.record(
                auditAction,
                "INVESTIGATION_CASE",
                savedCase.getId(),
                authenticatedAdmin,
                "Resolution " + decision.name().toLowerCase()
        );

        return mapToResponse(savedCase);
    }
}