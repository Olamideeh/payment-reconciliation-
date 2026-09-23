package com.example.payrecon;

import com.example.payrecon.dto.ReviewResolutionRequest;
import com.example.payrecon.dto.StartInvestigationRequest;
import com.example.payrecon.dto.SubmitResolutionRequest;
import com.example.payrecon.entity.InvestigationCase;
import com.example.payrecon.entity.ReconciliationResult;
import com.example.payrecon.enums.AuditAction;
import com.example.payrecon.enums.CaseStatus;
import com.example.payrecon.enums.ReconciliationStatus;
import com.example.payrecon.exception.ResourceNotFoundException;
import com.example.payrecon.repository.InvestigationCaseRepository;
import com.example.payrecon.service.AuditService;
import com.example.payrecon.service.InvestigationCaseService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InvestigationCaseServiceTest {

    @Mock
    private InvestigationCaseRepository caseRepository;

    @Mock
    private AuditService auditService;

    @InjectMocks
    private InvestigationCaseService caseService;

    @Test
    void shouldStartAnOpenInvestigation() {

        // Arrange
        ReconciliationResult result = new ReconciliationResult();
        result.setId(10L);
        result.setReference("TXN002");
        result.setStatus(
                ReconciliationStatus.AMOUNT_MISMATCH
        );

        InvestigationCase investigationCase =
                new InvestigationCase();

        investigationCase.setId(1L);
        investigationCase.setStatus(CaseStatus.OPEN);
        investigationCase.setReconciliationResult(result);

        StartInvestigationRequest request =
                new StartInvestigationRequest();

        request.setInvestigationNote(
                "Checking the provider transaction"
        );

        when(caseRepository.findById(1L))
                .thenReturn(Optional.of(investigationCase));

        when(caseRepository.save(any(InvestigationCase.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0)
                );

        // Act
        caseService.startInvestigation(
                1L,
                "operations.officer",
                request
        );

        // Assert
        assertEquals(
                CaseStatus.UNDER_REVIEW,
                investigationCase.getStatus()
        );

        assertEquals(
                "operations.officer",
                investigationCase.getInvestigatedBy()
        );

        assertEquals(
                "Checking the provider transaction",
                investigationCase.getInvestigationNote()
        );

        verify(caseRepository).findById(1L);
        verify(caseRepository).save(investigationCase);

        verify(auditService).record(
                AuditAction.INVESTIGATION_STARTED,
                "INVESTIGATION_CASE",
                1L,
                "operations.officer",
                "Investigation started"
        );
    }
    @Test
    void shouldNotStartInvestigationWhenCaseIsNotOpen() {

        // Arrange
        InvestigationCase investigationCase =
                new InvestigationCase();

        investigationCase.setId(1L);
        investigationCase.setStatus(
                CaseStatus.PENDING_APPROVAL
        );

        StartInvestigationRequest request =
                new StartInvestigationRequest();

        request.setInvestigationNote(
                "Trying to investigate again"
        );

        when(caseRepository.findById(1L))
                .thenReturn(Optional.of(investigationCase));

        // Act
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> caseService.startInvestigation(
                        1L,
                        "operations.officer",
                        request
                )
        );

        // Assert
        assertEquals(
                "Only an OPEN case can be investigated",
                exception.getMessage()
        );

        verify(caseRepository, never())
                .save(any(InvestigationCase.class));

        verifyNoInteractions(auditService);
    }
    @Test
    void shouldSubmitResolutionForAdminApproval() {

        // Arrange
        ReconciliationResult result = new ReconciliationResult();
        result.setId(10L);
        result.setReference("TXN002");
        result.setStatus(
                ReconciliationStatus.AMOUNT_MISMATCH
        );

        InvestigationCase investigationCase =
                new InvestigationCase();

        investigationCase.setId(1L);
        investigationCase.setStatus(CaseStatus.UNDER_REVIEW);
        investigationCase.setReconciliationResult(result);

        SubmitResolutionRequest request =
                new SubmitResolutionRequest(
                        "Update the internal amount"
                );

        when(caseRepository.findById(1L))
                .thenReturn(Optional.of(investigationCase));

        when(caseRepository.save(any(InvestigationCase.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0)
                );

        // Act
        caseService.submitResolution(
                1L,
                "operations.officer",
                request
        );

        // Assert
        assertEquals(
                CaseStatus.PENDING_APPROVAL,
                investigationCase.getStatus()
        );

        assertEquals(
                "Update the internal amount",
                investigationCase.getProposedResolution()
        );

        assertEquals(
                "operations.officer",
                investigationCase.getSubmittedBy()
        );

        verify(caseRepository).save(investigationCase);

        verify(auditService).record(
                AuditAction.RESOLUTION_SUBMITTED,
                "INVESTIGATION_CASE",
                1L,
                "operations.officer",
                "Resolution submitted for Admin approval"
        );
    }
    @Test
    void shouldAllowAdminToApprovePendingResolution() {

        // Arrange
        ReconciliationResult result = new ReconciliationResult();
        result.setId(10L);
        result.setReference("TXN002");
        result.setStatus(
                ReconciliationStatus.AMOUNT_MISMATCH
        );

        InvestigationCase investigationCase =
                new InvestigationCase();

        investigationCase.setId(1L);
        investigationCase.setStatus(
                CaseStatus.PENDING_APPROVAL
        );
        investigationCase.setSubmittedBy(
                "operations.officer"
        );
        investigationCase.setReconciliationResult(result);

        ReviewResolutionRequest request =
                new ReviewResolutionRequest(
                        "Evidence verified and approved"
                );

        when(caseRepository.findById(1L))
                .thenReturn(Optional.of(investigationCase));

        when(caseRepository.save(any(InvestigationCase.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0)
                );

        // Act
        caseService.approveResolution(
                1L,
                "admin.user",
                request
        );

        // Assert
        assertEquals(
                CaseStatus.APPROVED,
                investigationCase.getStatus()
        );

        assertEquals(
                "admin.user",
                investigationCase.getReviewedBy()
        );

        assertEquals(
                "Evidence verified and approved",
                investigationCase.getReviewComment()
        );

        verify(caseRepository).save(investigationCase);

        verify(auditService).record(
                AuditAction.RESOLUTION_APPROVED,
                "INVESTIGATION_CASE",
                1L,
                "admin.user",
                "Resolution approved"
        );
    }
    @Test
    void shouldPreventMakerFromApprovingOwnResolution() {

        // Arrange
        InvestigationCase investigationCase =
                new InvestigationCase();

        investigationCase.setId(1L);
        investigationCase.setStatus(
                CaseStatus.PENDING_APPROVAL
        );

        investigationCase.setSubmittedBy(
                "admin.user"
        );

        ReviewResolutionRequest request =
                new ReviewResolutionRequest(
                        "Trying to approve my own work"
                );

        when(caseRepository.findById(1L))
                .thenReturn(Optional.of(investigationCase));

        // Act
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> caseService.approveResolution(
                        1L,
                        "admin.user",
                        request
                )
        );

        // Assert
        assertEquals(
                "The maker cannot approve or reject their own resolution",
                exception.getMessage()
        );

        verify(caseRepository, never())
                .save(any(InvestigationCase.class));

        verifyNoInteractions(auditService);
    }

    @Test
    void shouldThrowNotFoundWhenInvestigationCaseDoesNotExist() {

        // Arrange
        Long missingCaseId = 999L;

        StartInvestigationRequest request =
                new StartInvestigationRequest();

        request.setInvestigationNote(
                "Checking transaction"
        );

        when(caseRepository.findById(missingCaseId))
                .thenReturn(Optional.empty());

        // Act
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> caseService.startInvestigation(
                        missingCaseId,
                        "operations.officer",
                        request
                )
        );

        // Assert
        assertEquals(
                "Investigation case not found: 999",
                exception.getMessage()
        );



        verify(caseRepository).findById(missingCaseId);

        verify(caseRepository, never())
                .save(any(InvestigationCase.class));

        verifyNoInteractions(auditService);
    }
}