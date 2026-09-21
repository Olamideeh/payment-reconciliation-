package com.example.payrecon.dto;

import com.example.payrecon.enums.BatchStatus;

public record ReconciliationSummaryResponse(
        Long batchId,
        BatchStatus batchStatus,
        long totalResults,
        long matched,
        long amountMismatches,
        long statusMismatches,
        long missingFromProvider,
        long missingInternally,
        long duplicates,
        long requiresInvestigation,
        double matchRatePercentage
) {
}