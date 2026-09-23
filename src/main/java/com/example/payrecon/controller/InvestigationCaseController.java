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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

@RestController
@RequestMapping("/api/investigation-cases")
@RequiredArgsConstructor
@Tag(
        name = "Investigation Cases",
        description = "Maker-checker workflow for unresolved reconciliation cases"
)
public class InvestigationCaseController {

    private final InvestigationCaseService caseService;
    @Operation(
            summary = "List investigation cases",
            description = "Admin and Operations Officer can filter and paginate cases"
    )
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
    @Operation(
            summary = "Start an investigation",
            description = "Operations Officer moves an OPEN case to UNDER_REVIEW"
    )
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
    @Operation(
            summary = "Submit a proposed resolution",
            description = "Operations Officer submits a case for Admin approval"
    )
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
    @Operation(
            summary = "Approve a resolution",
            description = "Admin approves a resolution waiting for approval"
    )
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
    @Operation(
            summary = "Reject a resolution",
            description = "Admin rejects a resolution waiting for approval"
    )
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