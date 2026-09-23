package com.example.payrecon.controller;

import com.example.payrecon.dto.BatchUploadResponse;
import com.example.payrecon.entity.ReconciliationBatch;
import com.example.payrecon.service.ReconciliationUploadService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.example.payrecon.dto.ReconciliationResultResponse;
import com.example.payrecon.entity.ReconciliationResult;
import com.example.payrecon.entity.TransactionRecord;
import com.example.payrecon.service.ReconciliationEngineService;
import com.example.payrecon.dto.ReconciliationSummaryResponse;
import com.example.payrecon.service.ReconciliationQueryService;
import java.util.List;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
@RestController
@RequestMapping("/api/reconciliations")
@RequiredArgsConstructor
@Tag(
        name = "Reconciliation",
        description = "Upload transaction files, run reconciliation and view results"
)
public class ReconciliationController {

    private final ReconciliationUploadService uploadService;
    private final ReconciliationEngineService engineService;
    private final ReconciliationQueryService queryService;
    @Operation(
            summary = "Upload reconciliation files",
            description = "Operations Officer uploads internal and provider CSV files"
    )
    @PostMapping(
            value = "/upload",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<BatchUploadResponse> uploadFiles(
            @RequestParam("internalFile")
            MultipartFile internalFile,

            @RequestParam("providerFile")
            MultipartFile providerFile,

            @RequestParam("uploadedBy")
            String uploadedBy
    ){

        ReconciliationBatch batch = uploadService.upload(
                internalFile,
                providerFile,
                uploadedBy
        );

        BatchUploadResponse response =
                new BatchUploadResponse(
                        batch.getId(),
                        batch.getInternalFileName(),
                        batch.getProviderFileName(),
                        batch.getUploadedBy(),
                        batch.getStatus(),
                        batch.getCreatedAt(),
                        "CSV files uploaded successfully"
                );

        return new ResponseEntity<>(
                response,
                HttpStatus.CREATED
        );
    }
    @Operation(
            summary = "Run reconciliation",
            description = "Compares internal and provider transactions for the selected batch"
    )
    @PostMapping("/{batchId}/run")
    public ResponseEntity<List<ReconciliationResultResponse>>
    runReconciliation(@PathVariable Long batchId) {

        List<ReconciliationResult> results =
                engineService.reconcile(batchId);

        List<ReconciliationResultResponse> response =
                results.stream()
                        .map(this::mapToResponse)
                        .toList();

        return ResponseEntity.ok(response);

    }
    private ReconciliationResultResponse mapToResponse(
            ReconciliationResult result
    ) {

        TransactionRecord internal =
                result.getInternalTransaction();

        TransactionRecord provider =
                result.getProviderTransaction();

        return new ReconciliationResultResponse(
                result.getId(),
                result.getReference(),

                internal == null ? null : internal.getAmount(),
                internal == null ? null : internal.getStatus(),

                provider == null ? null : provider.getAmount(),
                provider == null ? null : provider.getStatus(),

                result.getStatus(),
                result.isRequiresInvestigation()
        );
    }
    @Operation(
            summary = "View reconciliation summary",
            description = "Returns totals, match rate and mismatch counts for a batch"
    )
    @GetMapping("/{batchId}/summary")
    public ResponseEntity<ReconciliationSummaryResponse>
    getSummary(@PathVariable Long batchId) {

        return ResponseEntity.ok(
                queryService.getSummary(batchId)
        );
    }
}