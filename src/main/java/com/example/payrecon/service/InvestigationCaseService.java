package com.example.payrecon.service;

import com.example.payrecon.dto.InvestigationCaseResponse;
import com.example.payrecon.entity.InvestigationCase;
import com.example.payrecon.entity.ReconciliationResult;
import com.example.payrecon.enums.CaseStatus;
import com.example.payrecon.repository.InvestigationCaseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.payrecon.dto.StartInvestigationRequest;
import java.util.List;
import com.example.payrecon.dto.SubmitResolutionRequest;
import com.example.payrecon.dto.ReviewResolutionRequest;
@Service
@RequiredArgsConstructor
public class InvestigationCaseService {

    private final InvestigationCaseRepository caseRepository;

    @Transactional(readOnly = true)
    public List<InvestigationCaseResponse> getCases(
            CaseStatus status
    ) {

        List<InvestigationCase> cases = status == null
                ? caseRepository.findAll()
                : caseRepository.findAllByStatus(status);

        return cases.stream()
                .map(this::mapToResponse)
                .toList();
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
            StartInvestigationRequest request
    ) {

        InvestigationCase investigationCase =
                caseRepository.findById(caseId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Investigation case not found"
                                )
                        );

        if (investigationCase.getStatus() != CaseStatus.OPEN) {
            throw new IllegalStateException(
                    "Only an OPEN case can be investigated"
            );
        }

        investigationCase.setStatus(CaseStatus.UNDER_REVIEW);
        investigationCase.setInvestigationNote(
                request.investigationNote()
        );
        investigationCase.setInvestigatedBy(
                request.officer()
        );

        InvestigationCase savedCase =
                caseRepository.save(investigationCase);

        return mapToResponse(savedCase);
    }

    @Transactional
    public InvestigationCaseResponse submitResolution(
            Long caseId,
            SubmitResolutionRequest request
    ) {

        InvestigationCase investigationCase =
                caseRepository.findById(caseId)
                        .orElseThrow(() ->
                                new RuntimeException(
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

        investigationCase.setSubmittedBy(
                request.submittedBy()
        );

        investigationCase.setStatus(
                CaseStatus.PENDING_APPROVAL
        );

        InvestigationCase savedCase =
                caseRepository.save(investigationCase);

        return mapToResponse(savedCase);
    }
    @Transactional
    public InvestigationCaseResponse approveResolution(
            Long caseId,
            ReviewResolutionRequest request
    ) {

        return reviewResolution(
                caseId,
                request,
                CaseStatus.APPROVED
        );
    }

    @Transactional
    public InvestigationCaseResponse rejectResolution(
            Long caseId,
            ReviewResolutionRequest request
    ) {

        return reviewResolution(
                caseId,
                request,
                CaseStatus.REJECTED
        );
    }

    private InvestigationCaseResponse reviewResolution(
            Long caseId,
            ReviewResolutionRequest request,
            CaseStatus decision
    ) {

        InvestigationCase investigationCase =
                caseRepository.findById(caseId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Investigation case not found"
                                )
                        );

        if (investigationCase.getStatus()
                != CaseStatus.PENDING_APPROVAL) {

            throw new IllegalStateException(
                    "Only a pending case can be reviewed"
            );
        }

        if (investigationCase.getSubmittedBy()
                .equalsIgnoreCase(request.reviewedBy())) {

            throw new IllegalStateException(
                    "The maker cannot approve or reject their own resolution"
            );
        }

        investigationCase.setStatus(decision);
        investigationCase.setReviewedBy(request.reviewedBy());
        investigationCase.setReviewComment(
                request.reviewComment()
        );

        InvestigationCase savedCase =
                caseRepository.save(investigationCase);

        return mapToResponse(savedCase);
    }
}