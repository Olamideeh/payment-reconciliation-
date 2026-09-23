package com.example.payrecon.controller;

import com.example.payrecon.dto.InvestigationCaseResponse;
import com.example.payrecon.dto.ReviewResolutionRequest;
import com.example.payrecon.dto.StartInvestigationRequest;
import com.example.payrecon.dto.SubmitResolutionRequest;
import com.example.payrecon.enums.CaseStatus;
import com.example.payrecon.service.InvestigationCaseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/investigation-cases")
@RequiredArgsConstructor
public class InvestigationCaseController {

    private final InvestigationCaseService caseService;

    @GetMapping
    public ResponseEntity<Page<InvestigationCaseResponse>> getCases(
            @RequestParam(required = false)
            CaseStatus status,

            @RequestParam(defaultValue = "0")
            int page,

            @RequestParam(defaultValue = "20")
            int size
    ) {

        return ResponseEntity.ok(
                caseService.getCases(
                        status,
                        page,
                        size
                )
        );
    }
    @PatchMapping("/{caseId}/start")
    public ResponseEntity<InvestigationCaseResponse> startInvestigation(
            @PathVariable Long caseId,
            @Valid @RequestBody StartInvestigationRequest request,
            Authentication authentication
    ) {

        return ResponseEntity.ok(
                caseService.startInvestigation(
                        caseId,
                        authentication.getName(),
                        request
                )
        );
    }

    @PatchMapping("/{caseId}/submit")
    public ResponseEntity<InvestigationCaseResponse> submitResolution(
            @PathVariable Long caseId,
            @Valid @RequestBody SubmitResolutionRequest request,
            Authentication authentication
    ) {

        return ResponseEntity.ok(
                caseService.submitResolution(
                        caseId,
                        authentication.getName(),
                        request
                )
        );
    }

    @PatchMapping("/{caseId}/approve")
    public ResponseEntity<InvestigationCaseResponse> approveResolution(
            @PathVariable Long caseId,
            @Valid @RequestBody ReviewResolutionRequest request,
            Authentication authentication
    ) {

        return ResponseEntity.ok(
                caseService.approveResolution(
                        caseId,
                        authentication.getName(),
                        request
                )
        );
    }

    @PatchMapping("/{caseId}/reject")
    public ResponseEntity<InvestigationCaseResponse> rejectResolution(
            @PathVariable Long caseId,
            @Valid @RequestBody ReviewResolutionRequest request,
            Authentication authentication
    ) {

        return ResponseEntity.ok(
                caseService.rejectResolution(
                        caseId,
                        authentication.getName(),
                        request
                )
        );
    }
}