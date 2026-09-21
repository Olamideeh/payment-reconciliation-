package com.example.payrecon.service;

import com.example.payrecon.dto.ReconciliationSummaryResponse;
import com.example.payrecon.entity.ReconciliationBatch;
import com.example.payrecon.entity.ReconciliationResult;
import com.example.payrecon.enums.ReconciliationStatus;
import com.example.payrecon.repository.ReconciliationBatchRepository;
import com.example.payrecon.repository.ReconciliationResultRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReconciliationQueryService {

    private final ReconciliationBatchRepository batchRepository;
    private final ReconciliationResultRepository resultRepository;

    @Transactional(readOnly = true)
    public ReconciliationSummaryResponse getSummary(Long batchId) {

        ReconciliationBatch batch = batchRepository.findById(batchId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Reconciliation batch not found"
                        )
                );

        List<ReconciliationResult> results =
                resultRepository.findAllByBatch_Id(batchId);

        long total = results.size();

        long matched = countStatus(
                results,
                ReconciliationStatus.MATCHED
        );

        long amountMismatches = countStatus(
                results,
                ReconciliationStatus.AMOUNT_MISMATCH
        );

        long statusMismatches = countStatus(
                results,
                ReconciliationStatus.STATUS_MISMATCH
        );

        long missingFromProvider = countStatus(
                results,
                ReconciliationStatus.MISSING_FROM_PROVIDER
        );

        long missingInternally = countStatus(
                results,
                ReconciliationStatus.MISSING_INTERNALLY
        );

        long duplicates = countStatus(
                results,
                ReconciliationStatus.DUPLICATE
        );

        long requiresInvestigation = results.stream()
                .filter(ReconciliationResult::isRequiresInvestigation)
                .count();

        double matchRate = total == 0
                ? 0
                : (matched * 100.0) / total;

        matchRate = Math.round(matchRate * 100.0) / 100.0;

        return new ReconciliationSummaryResponse(
                batch.getId(),
                batch.getStatus(),
                total,
                matched,
                amountMismatches,
                statusMismatches,
                missingFromProvider,
                missingInternally,
                duplicates,
                requiresInvestigation,
                matchRate
        );
    }

    private long countStatus(
            List<ReconciliationResult> results,
            ReconciliationStatus status
    ) {
        return results.stream()
                .filter(result -> result.getStatus() == status)
                .count();
    }
}